package co.bdozer.ai.server

import co.bdozer.core.nlp.sdk.ApiClient
import co.bdozer.core.nlp.sdk.api.DefaultApi
import co.bdozer.core.nlp.sdk.model.DocInput
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class Edgar10KParser {

    private val secEndpoint = "https://www.sec.gov"
    private val rss =
        "$secEndpoint/cgi-bin/browse-edgar?action=getcurrent&type=10-k&company=&dateb=&owner=include&start=0&count=40&output=atom"
    private val rssConnection = Jsoup.connect(rss)
    private final val apiClient = ApiClient()
    private val log = LoggerFactory.getLogger(Edgar10KParser::class.java)

    init {
        apiClient.basePath = "http://localhost:8000"
    }

    private val nlpApi = apiClient.buildClient(DefaultApi::class.java)

    fun readLatestFilingRss() {
        val document = rssConnection.get()
        val hrefs = document
            .select("entry")
            .mapNotNull { entry ->
                val links = entry.select("link")
                val link = links.first()
                link?.attr("href")
            }
            .take(2)

        val cikAshes = hrefs.mapNotNull { href -> extractCikAsh(href) }

        val parsedDocuments = cikAshes.map { (cik, ash) ->
            val original10K = original10K(cik, ash)
            val docUrl =
                "$secEndpoint/Archives/edgar/data/$cik/${ash.padStart(length = 18, padChar = '0')}/$original10K"
            log.info(
                "Retrieving document from docUrl={}, cik={}, ash={}",
                docUrl,
                cik,
                ash,
            )
            val tenK = Jsoup.connect(docUrl).get()
            val spans = spans(tenK)

            val start = System.currentTimeMillis()
            log.info("Calling NLP server with spans size={}", spans.size)
            val sentences = spans.flatMap { span ->
                nlpApi.getSentences(DocInput().doc(span)).sentences
            }
            val stop = System.currentTimeMillis()
            log.info(
                "NLP server returned {} sentences in {}s",
                sentences.size,
                TimeUnit.SECONDS.convert(stop - start, TimeUnit.MILLISECONDS),
            )

            ParsedDocument(
                cik = cik,
                ash = ash,
                docUrl = docUrl,
                lines = sentences
            )
        }
        // TODO determine what to do w/ the parsed documents and what other metadata we might want to store on it
    }

    private fun spans(doc: Document): List<String> {
        return doc.root()
            .select("span")
            .filter {
                val isNotEmpty = it.text().isNotEmpty()
                val tokens = it.text().split(" ")
                isNotEmpty && tokens.size > 1
            }
            .map { it.text() }
    }

    private fun extractCikAsh(url: String): CikAsh? {
        val regex = "$secEndpoint/Archives/edgar/data/(\\d+)/(\\d+)/.*".toRegex()
        val matchResult = regex.matchEntire(url)
        val groups = matchResult?.groups
        val cik = groups?.get(1)?.value
        val ash = groups?.get(2)?.value

        return if (cik != null && ash != null)
            CikAsh(cik, ash)
        else null
    }

    private fun original10K(cik: String, ash: String): String? {
        val filingSummary =
            "$secEndpoint/Archives/edgar/data/${cik}/${ash.padStart(length = 18, padChar = '0')}/FilingSummary.xml"
        val doc = Jsoup.connect(filingSummary).get()
        // find the main file instance
        return doc
            .select("InputFiles")
            .select("File")
            .find { it.attr("doctype") == "10-K" }
            ?.text()
    }

}