package co.bdozer

import co.bdozer.models.CompanyTicker
import co.bdozer.models.Submission
import co.bdozer.sectionparser.Section
import co.bdozer.sectionparser.SectionParser
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
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayDeque

val log: Logger = LoggerFactory.getLogger("Driver")

val objectMapper: ObjectMapper =
    jacksonObjectMapper()
        .findAndRegisterModules()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

val httpHost = HttpHost("localhost", 9200)
val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
val restHighLevelClient = RestHighLevelClient(RestClient.builder(httpHost))
val tickerMap = objectMapper.readValue<Map<String, CompanyTicker>>(FileInputStream("ten-k-parser/company_tickers.json"))

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
    val sectionParser = SectionParser()
    val sections = sectionParser.findSections(doc)
    val elements = filterDownToSection(doc, sections.business)
    removeTables(elements)
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

fun getSubmission(cik: String): Submission {
    log.info("CIK=$cik")
    val inputStream = HttpClient.newHttpClient().send(
        HttpRequest.newBuilder().GET().uri(URI.create("https://data.sec.gov/submissions/CIK${cik}.json")).build(),
        HttpResponse.BodyHandlers.ofInputStream(),
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

fun removeTables(elements: List<Element>) {
    elements.forEach { removeTables(it) }
}

/**
 * Perform a DFT on the element and it's children
 * when we see a <table></table> be sure to get rid of it
 * from it DOM
 */
fun removeTables(element: Element) {
    val stack = Stack<Element>()
    stack.addAll(element.children())
    while (stack.isNotEmpty()) {
        val elm = stack.pop()
        if (elm.tagName() == "table") {
            elm.remove()
        } else {
            stack.addAll(elm.children())
        }
    }
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