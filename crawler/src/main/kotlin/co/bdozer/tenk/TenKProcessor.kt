package co.bdozer.tenk

import co.bdozer.utils.HashGenerator.hash
import co.bdozer.utils.HtmlToPlainText
import co.bdozer.tenk.models.CompanyTicker
import co.bdozer.tenk.models.Submission
import co.bdozer.tenk.models.TenK
import co.bdozer.tenk.sectionparser.TenKSectionExtractor
import co.bdozer.utils.Beans
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
import org.elasticsearch.action.index.IndexRequest
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

    private val tickerMap =
        objectMapper.readValue<Map<String, CompanyTicker>>(FileInputStream("crawler/company_tickers.json"))

    private fun submission(cik: String): Submission {
        val inputStream = HttpClient
            .newHttpClient()
            .send(
                HttpRequest
                    .newBuilder()
                    .GET()
                    .uri(URI.create("https://data.sec.gov/submissions/CIK${cik}.json"))
                    .build(),
                HttpResponse
                    .BodyHandlers
                    .ofInputStream(),
            )
            .body()
        return objectMapper.readValue(inputStream)
    }

    fun processTicker(ticker: String) {

        val company = tickerMap.entries.find { it.value.ticker == ticker }?.value ?: error("cannot find ticker $ticker")
        val cik = company.cik_str.padStart(length = 10, padChar = '0')

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
        chunks.filter { it.isNotBlank() }.forEachIndexed { seqNo, chunk ->
            val id = hash(url, "business", seqNo.toString())
            val section = "Business"
            val tenK = TenK(
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

            val json = objectMapper.writeValueAsString(tenK)
            val indexResponse = restHighLevelClient.index(
                IndexRequest("ten-k")
                    .id(id)
                    .source(json, XContentType.JSON),
                RequestOptions.DEFAULT,
            )
            
            log.info("Indexed document, result={}, ticker={} seqNo={}", indexResponse.result, ticker, seqNo)
        }

    }

}
