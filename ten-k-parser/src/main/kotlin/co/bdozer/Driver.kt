package co.bdozer

import co.bdozer.models.CompanyTicker
import co.bdozer.models.Recent
import co.bdozer.models.Submission
import co.bdozer.sectionparser.Section
import co.bdozer.sectionparser.SectionParser
import com.fasterxml.jackson.databind.DeserializationFeature
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
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.security.MessageDigest
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.arrayListOf
import kotlin.collections.emptyList
import kotlin.collections.find
import kotlin.collections.forEach
import kotlin.collections.indexOfFirst
import kotlin.collections.indices
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.system.exitProcess

private val log: Logger = LoggerFactory.getLogger("Driver")

private val objectMapper =
    jacksonObjectMapper().findAndRegisterModules().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

private val restHighlevelClient = RestHighLevelClient(RestClient.builder(HttpHost("localhost", 9200)))

fun main() {
    val tickers = FileInputStream("ten-k-parser/tickers.txt")
        .bufferedReader()
        .readLines()
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val start = System.currentTimeMillis()
    val tickerMap =
        objectMapper.readValue<Map<String, CompanyTicker>>(FileInputStream("ten-k-parser/company_tickers.json"))
    val end = System.currentTimeMillis()
    log.info("Loading company tickers took ${end - start}ms")

    for (ticker in tickers) {
        try {
            processSingleCompany(tickerMap, ticker)
        } catch (e: Exception) {
            log.error("Exception occurred while processing ticker={}, error={}", ticker, e.message)
        }
    }
    exitProcess(0)
}

private fun processSingleCompany(
    tickerMap: Map<String, CompanyTicker>, ticker: String
) {

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
    log.info("Parsing form=$form cik=$cik ash=$ash primaryDocument=$primaryDocument latest10K=${url}")

    val doc = Jsoup.connect(url).get()
    val sectionParser = SectionParser()
    val sections = sectionParser.findSections(doc)
    val elements = filterDownToSection(doc, sections.business)
    val body = Element("body")
    elements.forEach { body.appendChild(it) }

    log.info("bodySize={}", body.toString().utf8Size())
    val textBody = HttpClient.newHttpClient().send(
        HttpRequest.newBuilder().POST(BodyPublishers.ofString(body.toString())).header("Content-Type", "text/plain")
            .uri(URI.create("http://localhost:3000/convert")).build(),
        BodyHandlers.ofString(),
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
    val indexResponse = restHighlevelClient.index(
        IndexRequest("filings").source(objectMapper.writeValueAsString(esFiling), XContentType.JSON),
        RequestOptions.DEFAULT,
    )
    log.info("Indexed document, result={}, ticker={}", indexResponse.result, ticker)
}

val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
fun hash(vararg components: String): String {
    val str = components.joinToString()
    return bytesToHex(digest.digest(str.encodeToByteArray()))
}

fun bytesToHex(hash: ByteArray): String {
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
    log.info("CIK=$cik")
    val inputStream = HttpClient.newHttpClient().send(
        HttpRequest.newBuilder().GET().uri(URI.create("https://data.sec.gov/submissions/CIK${cik}.json")).build(),
        BodyHandlers.ofInputStream(),
    ).body()
    return objectMapper.readValue(inputStream)
}

fun containsEndAnchor(endAnchor: String, root: Element): Boolean {
    if (root.id() == endAnchor.replaceFirst("#", "")) {
        return true
    }

    val stack = Stack<Element>()
    stack.addAll(root.children())

    while (stack.isNotEmpty()) {
        val element = stack.pop()
        if (element.id() == endAnchor.replaceFirst("#", "")) {
            return true
        }
        stack.addAll(element.children())
    }
    return false
}

fun filterDownToSection(
    doc: Document,
    section: Section,
): List<Element> {
    val startAnchor = section.startAnchor ?: return emptyList()
    val endAnchor = section.endAnchor ?: return emptyList()

    val start = doc.getElementById(startAnchor.replaceFirst("#", "")) ?: return emptyList()

    val queue = ArrayDeque<Element>()
    queue.add(start)

    val ret = arrayListOf<Element>()

    var endAnchorFound = false
    while (!endAnchorFound && queue.isNotEmpty()) {
        val element = queue.removeFirst()
        ret.add(element)
        if (containsEndAnchor(endAnchor, element)) {
            endAnchorFound = true
        } else {
            if (queue.isEmpty()) {
                queue.addAll(ancestorSiblings(element))
            }
        }
    }

    return ret
}

fun ancestorSiblings(element: Element): List<Element> {
    var current: Element? = element
    while (current?.parent() != null && current.nextElementSiblings().isEmpty()) {
        current = current.parent()
    }
    return current?.nextElementSiblings() ?: emptyList()
}