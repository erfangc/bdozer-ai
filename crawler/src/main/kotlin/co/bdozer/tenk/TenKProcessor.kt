package co.bdozer.tenk

import co.bdozer.utils.HashGenerator.hash
import co.bdozer.utils.HtmlToPlainText
import co.bdozer.tenk.models.CompanyTicker
import co.bdozer.tenk.models.Submission
import co.bdozer.tenk.models.TenK
import co.bdozer.tenk.sectionparser.TenKSectionExtractor
import co.bdozer.utils.Beans
import co.bdozer.utils.Database
import co.bdozer.utils.Database.runSql
import co.bdozer.utils.DocumentChunker
import co.bdozer.utils.DocumentChunker.chunkDoc
import co.bdozer.utils.HtmlToPlainText.plainText
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okio.utf8Size
import org.apache.http.HttpHost
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkRequestBuilder
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate

object TenKProcessor {

    private val log = LoggerFactory.getLogger(TenKProcessor::class.java)
    private val objectMapper = Beans.objectMapper()
    private val restHighLevelClient = Beans.restHighLevelClient()

    private fun submission(cik: String): Submission {
        val inputStream = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().GET().uri(URI.create("https://data.sec.gov/submissions/CIK${cik}.json")).build(),
            HttpResponse.BodyHandlers.ofInputStream(),
        ).body()
        return objectMapper.readValue(inputStream)
    }

    private fun toCik(ticker: String): String {
        val row = runSql(
            sql = """
            select cik from ids where ticker = '$ticker'
        """.trimIndent()
        ).first()
        val cik = row["cik"]?.toString() ?: error("cannot find cik for ticker $ticker")
        log.info("Resolved ticker $ticker to cik $cik")
        return cik
    }

    fun processTicker(ticker: String): List<TenK> {

        val cik = toCik(ticker)

        // 
        // Find the latest submission and print out the raw text
        // 
        val submission = submission(cik)

        val form = "10-K"
        val idx = submission.filings?.recent?.form?.indexOfFirst { it == form }
            ?: error("cannot find form $form for ticker $ticker")
        val recent = submission.filings.recent
        val ash = recent.accessionNumber?.get(idx) ?: error("...")
        val reportDate = recent.reportDate?.get(idx) ?: error("...")

        val primaryDocument = recent.primaryDocument?.get(idx) ?: error("...")
        val url = "https://www.sec.gov/Archives/edgar/data/$cik/${ash.replace("-", "")}/$primaryDocument"
        log.info("Parsing form=$form cik=$cik ash=$ash primaryDocument=$primaryDocument url=${url} ")

        val doc = Jsoup.connect(url).get()
        val tenKSectionExtractor = TenKSectionExtractor()
        val sections = tenKSectionExtractor.extractSections(doc)
        val elements = sections.business.elements
        val body = Element("body")
        elements.forEach { body.appendChild(it) }

        val textBody = plainText(body)

        //
        // Chunk the text into sections
        //
        val chunks = chunkDoc(textBody)

        //
        // Index each section
        //
        return chunks.filter { it.isNotBlank() }.mapIndexed { seqNo, chunk ->
            val id = hash(url, "business", seqNo.toString())
            val section = "Business"
            TenK(
                id = id,
                cik = cik,
                ash = ash,
                url = url,
                seqNo = seqNo,
                text = chunk,
                section = section,
                ticker = ticker,
                reportDate = LocalDate.parse(reportDate),
                companyName = submission.name ?: "Unknown",
            )
        }

    }

    fun indexTenKs(tenKs: List<TenK>) {
        val indexRequests = tenKs.map { tenK ->
            val json = objectMapper.writeValueAsString(tenK)
            IndexRequest("ten-k").id(tenK.id).source(json, XContentType.JSON)
        }

        val bulkRequest = BulkRequest()
        indexRequests.forEach { bulkRequest.add(it) }
        val indexResponse = restHighLevelClient.bulk(
            bulkRequest,
            RequestOptions.DEFAULT,
        )

        log.info(
            "BulkIndex complete, ingestTook=${indexResponse.ingestTook} items=${indexResponse.items.size}",
            indexResponse.ingestTook,
            indexResponse.items
        )
    }

}

