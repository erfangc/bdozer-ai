package co.bdozer.ai.server.services.rss

import co.bdozer.ai.server.services.rss.models.Category
import co.bdozer.ai.server.services.rss.models.CikAsh
import co.bdozer.ai.server.services.rss.models.Entry
import org.jsoup.Jsoup
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RssProcessor {

    private val secEndpoint = "https://www.sec.gov"
    private fun url(start: Int?, count: Int?): String {
        return "$secEndpoint/cgi-bin/browse-edgar" +
                "?action=getcurrent" +
                "&type=10-k" +
                "&company" +
                "=&dateb=" +
                "&owner=include" +
                "&start=${start ?: 0}" +
                "&count=${count ?: 40}" +
                "&output=atom"
    }

    fun entries(start: Int?, count: Int?): List<Entry> {
        val document = Jsoup.connect(url(start, count)).get()
        return document
            .select("entry")
            .mapNotNull { entry ->
                val link = entry.select("link").first()?.attr("href")
                val cikAsh = link?.let { cikAsh(link) }
                val category = entry.select("category").first()

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
                    cik = cikAsh?.cik,
                    ash = cikAsh?.ash,
                )
            }
    }

    private fun cikAsh(url: String): CikAsh? {
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