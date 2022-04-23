package co.bdozer

import org.jsoup.Jsoup
import java.net.URI

fun main() {
    val entry =
        URI.create("https://www.investopedia.com/articles/investing/052814/these-sectors-benefit-rising-interest-rates.asp")

    val links = traverse(entry.toString())
    val visited = hashSetOf(entry.toString())
    val queue = ArrayDeque<String>()
    val nextLinks = links.filter {
        URI.create(it).host == entry.host && !visited.contains(it)
    }
    queue.addAll(nextLinks)

    while (queue.isNotEmpty()) {
        println("Visited size=${visited.size} queue size=${queue.size}")
        for (link in queue) {
            visited.add(link)
            val children = traverse(link).filter {
                URI.create(it).host == entry.host && !visited.contains(it)
            }
            queue.addAll(children)
        }
    }

//    val documentBody = document.getElementById("article-body_1-0")
//    val formatter = HtmlToPlainText()
//    println(formatter.getPlainText(documentBody))
}

private fun traverse(uri: String): List<String> {
    println("Visiting $uri")
    val connect =
        Jsoup.connect(uri)
    val document = connect.get()
    val anchors = document.select("a")
    return anchors.map { it.attr("href") }
}
