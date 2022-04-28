package co.bdozer.investopedia

import co.bdozer.utils.Beans
import co.bdozer.utils.DocumentChunker
import co.bdozer.utils.HashGenerator.hash
import co.bdozer.utils.HtmlToPlainText.plainText
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.common.xcontent.XContentType
import org.jsoup.Jsoup
import java.net.URI

private val visited = hashSetOf<String>()
private val queue = ArrayDeque<String>()
private val entry: URI =
    URI.create(
        "https://www.investopedia.com/articles/investing/052814/these-sectors-benefit-rising-interest-rates.asp"
    )

private val restHighLevelClient = Beans.restHighLevelClient()
private val objectMapper = Beans.objectMapper()

fun main() {

    val nextLinks = visit(entry.toString())
    queue.addAll(nextLinks)

    while (queue.isNotEmpty()) {
        for (link in queue) {
            queue.remove(link)
            val children = visit(link)
            println("Link $link contains ${children.size} outgoing links")
            queue.addAll(children)
        }
    }

}

private fun visit(url: String): List<String> {
    try {
        visited.add(url)
        println("Visiting url=$url visited=${visited.size} queue=${queue.size}")

        val connect = Jsoup.connect(url)
        val document = connect.get()
        val documentBody = document.getElementById("article-body_1-0")
        val title = document.selectFirst("title")?.text()
        index(url, title, plainText(documentBody))
        val anchors = document.select("a")
        val hrefs = anchors.map { it.attr("href") }

        return hrefs.filter { href ->
            URI.create(href).host == entry.host
                    && !visited.contains(href)
                    && !queue.contains(href)
        }
    } catch (e: Exception) {
        println("Error occurred while visiting $url, error: ${e.message}")
        return emptyList()
    }
}

private fun index(url: String, title: String? = null, doc: String) {
    val chunks = DocumentChunker.chunkDoc(doc)

    val indexResponses = chunks.mapIndexed { seqNo, text ->
        val indexRequest = IndexRequest("investopedia")
        val id = hash(url, seqNo.toString())
        val obj = Investopedia(
            id = id,
            uri = url,
            seqNo = seqNo,
            text = text,
            title = title,
        )
        indexRequest
            .id(id)
            .source(objectMapper.writeValueAsString(obj), XContentType.JSON)
        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT)
    }

    val results = indexResponses.groupBy { it.result }.mapValues { it.value.size }
    println("Indexed url=$url, title=$title, chunks=${chunks.size}, results=$results")
}
