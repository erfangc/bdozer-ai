package co.bdozer.ai.server.services.rss.rss

import org.jsoup.Jsoup

class RssProcessor {
    private val secEndpoint = "https://www.sec.gov"
    private val rss =
        "$secEndpoint/cgi-bin/browse-edgar?action=getcurrent&type=10-k&company=&dateb=&owner=include&start=0&count=40&output=atom"
    private val rssConnection = Jsoup.connect(rss)
    
    fun init() {
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

}