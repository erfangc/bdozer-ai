package co.bdozer.rss

import co.bdozer.rss.models.Category
import co.bdozer.rss.models.CikAsh
import co.bdozer.rss.models.Entry
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.time.Instant

class RssProcessor {

    private val secEndpoint = "https://www.sec.gov"
    private val log = LoggerFactory.getLogger(RssProcessor::class.java)

    private fun url(start: Int?, count: Int?): String {
        return "$secEndpoint/cgi-bin/browse-edgar" +
                "?action=getcurrent" +
                "&type=10-k" +
                "&company" +
                "=&dateb=" +
                "&owner=include" +
                "&start=${start ?: 0}" +
                "&count=${count ?: 5}" +
                "&output=atom"
    }

    fun original10K(cik: String, ash: String): String {
        val filingSummaryUrl =
            "$secEndpoint/Archives/edgar/data/${cik}/${ash.padStart(length = 18, padChar = '0')}/FilingSummary.xml"
        val doc = Jsoup.connect(filingSummaryUrl).get()
        // find the main file instance
        return doc
            .select("InputFiles")
            .select("File")
            .find { it.attr("doctype") == "10-K" }
            ?.text()
            ?: error("Unable to find a FilingSummary.xml on $filingSummaryUrl")
    }

    fun entries(start: Int?, count: Int?): List<Entry> {
        val url = url(start, count)
        log.info("Fetching URL {}", url)
        val document = Jsoup.connect(url).get()
        return document
            .select("entry")
            .mapNotNull { entry ->
                try {
                    val link = entry.select("link").first()?.attr("href")
                    val cikAsh = link?.let { cikAsh(link) }
                    val category = entry.select("category").first()

                    val cik = cikAsh?.cik ?: error("...")
                    val ash = cikAsh.ash
                    Entry(
                        title = entry.select("title").text(),
                        id = entry.select("id").text(),
                        link = link,
                        updated = entry.select("updated").text().let { Instant.parse(it) },
                        summary = entry.select("summary").text(),
                        category = Category(
                            label = category?.attr("label"),
                            term = category?.attr("term"),
                        ),
                        original10K = original10K(cik = cik, ash = ash),
                        cik = cik,
                        ash = ash,
                    )
                } catch (e: Exception) {
                    log.error("Cannot create an entry for entry={}", entry.text(), e)
                    null
                }
            }
    }

    fun cikAsh(url: String): CikAsh? {

        val regex = "$secEndpoint/Archives/edgar/data/(\\d+)/(\\d+)/.*".toRegex()
        val matchResult = regex.matchEntire(url)
        val groups = matchResult?.groups
        val cik = groups?.get(1)?.value
        val ash = groups?.get(2)?.value

        return if (cik != null && ash != null) {
            CikAsh(cik, ash)
        } else {
            null
        }
    }

}