package co.bdozer

import co.bdozer.models.CompanyTicker
import co.bdozer.models.Submission
import co.bdozer.sectionparser.Section
import co.bdozer.sectionparser.SectionParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okio.utf8Size
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
import java.util.Stack

private val log: Logger = LoggerFactory.getLogger("Driver")

private val objectMapper =
    jacksonObjectMapper()
        .findAndRegisterModules()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

fun main() {
    val start = System.currentTimeMillis()
    val tickerMap =
        objectMapper.readValue<Map<String, CompanyTicker>>(FileInputStream("ten-k-parser/company_tickers.json"))
    val end = System.currentTimeMillis()

    val ticker = "ADBE"
    log.info("Loading company tickers took ${end - start}ms")
    val company = tickerMap.entries.find { it.value.ticker == ticker }?.value ?: error("...")
    val cik = company.cik_str.padStart(length = 10, padChar = '0')

    // ------------------------------------------------------
    // Find the latest submission and print out the raw text
    // ------------------------------------------------------
    val submission = submission(cik)
    val idx = submission.filings?.recent?.form?.indexOfFirst { it == "10-K" } ?: error("...")

    val ash = submission.filings.recent.accessionNumber?.get(idx) ?: error("...")
    val primaryDocument = submission.filings.recent.primaryDocument?.get(idx) ?: error("...")
    val latest10K = "https://www.sec.gov/Archives/edgar/data/$cik/${ash.replace("-", "")}/$primaryDocument"
    log.info("cik=$cik ash=$ash primaryDocument=$primaryDocument latest10K=${latest10K}")

    val doc = Jsoup.connect(latest10K).get()
    val sectionParser = SectionParser()
    val sections = sectionParser.findSections(doc)
    val elements = filterDownToSection(doc, sections.business)
    val body = Element("body")
    elements.forEach { body.appendChild(it) }

    log.info("bodySize={}", body.toString().utf8Size())
    val textBody = HttpClient.newHttpClient().send(
        HttpRequest
            .newBuilder()
            .POST(BodyPublishers.ofString(body.toString()))
            .header("Content-Type", "text/plain")
            .uri(URI.create("http://localhost:3000/convert"))
            .build(),
        BodyHandlers.ofString(),
    ).body()
    
    println(textBody)
}

private fun submission(cik: String): Submission {
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