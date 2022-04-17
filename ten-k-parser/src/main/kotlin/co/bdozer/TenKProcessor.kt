package co.bdozer

import co.bdozer.models.CompanyTicker
import co.bdozer.models.ESFiling
import co.bdozer.models.Submission
import co.bdozer.sectionparser.TenKSectionExtractor
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
import java.security.MessageDigest
import java.time.Instant

class TenKProcessor {
    private val log = LoggerFactory.getLogger(TenKProcessor::class.java)
    private val httpHost = HttpHost("localhost", 9200)
    private val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
    private val restHighLevelClient = RestHighLevelClient(RestClient.builder(httpHost))
    private val objectMapper: ObjectMapper =
        jacksonObjectMapper()
            .findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val tickerMap = objectMapper.readValue<Map<String, CompanyTicker>>(FileInputStream("ten-k-parser/company_tickers.json"))

    fun hash(vararg components: String): String {
        val str = components.joinToString()
        return bytesToHex(digest.digest(str.encodeToByteArray()))
    }

    private fun bytesToHex(hash: ByteArray): String {
        val hexString = StringBuilder(2 * hash.size)
        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }

    private fun getSubmission(cik: String): Submission {
        val inputStream = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().GET().uri(URI.create("https://data.sec.gov/submissions/CIK${cik}.json")).build(),
            HttpResponse.BodyHandlers.ofInputStream(),
        ).body()
        return objectMapper.readValue(inputStream)
    }

    fun processSingleCompany(ticker: String) {

        val company = tickerMap.entries.find { it.value.ticker == ticker }?.value ?: error("...")
        val cik = company.cik_str.padStart(length = 10, padChar = '0')

        // ------------------------------------------------------
        // Find the latest submission and print out the raw text
        // ------------------------------------------------------
        val submission = getSubmission(cik)

        val form = "10-K"
        val idx = submission.filings?.recent?.form?.indexOfFirst { it == form } ?: error("...")
        val recent = submission.filings.recent
        val ash = recent.accessionNumber?.get(idx) ?: error("...")
        val reportDate = recent.reportDate?.get(idx) ?: error("...")

        val primaryDocument = recent.primaryDocument?.get(idx) ?: error("...")
        val url = "https://www.sec.gov/Archives/edgar/data/$cik/${ash.replace("-", "")}/$primaryDocument"
        log.info("Parsing form=$form cik=$cik ash=$ash primaryDocument=$primaryDocument latest10K=${url} ")

        val doc = Jsoup.connect(url).get()
        val tenKSectionExtractor = TenKSectionExtractor()
        val sections = tenKSectionExtractor.extractSections(doc)
        val elements = sections.business.elements
        val body = Element("body")
        elements.forEach { body.appendChild(it) }

        log.info("Processed HTML bodySize={}", body.toString().utf8Size())
        val textBody = HttpClient.newHttpClient().send(
            HttpRequest
                .newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .header("Content-Type", "text/plain")
                .uri(URI.create("http://localhost:3000/convert")).build(),
            HttpResponse.BodyHandlers.ofString(),
        ).body()

        // ------------------------------
        // Put the extracted data into ES
        // ------------------------------
        val section = "Business"
        val esFiling = ESFiling(
            cik = cik,
            ash = ash,
            url = url,
            reportDate = reportDate,
            text = textBody,
            section = section,
            form = form,
            ticker = ticker,
            companyName = submission.name ?: "Unknown",
            timestamp = Instant.now().toString(),
        )

        val json = objectMapper.writeValueAsString(esFiling)
        val indexResponse = restHighLevelClient.index(
            IndexRequest("filings").id(hash(url, "business")).source(json, XContentType.JSON),
            RequestOptions.DEFAULT,
        )
        log.info("Indexed document, result={}, ticker={}", indexResponse.result, ticker)
    }

}