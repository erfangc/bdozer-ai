package co.bdozer.investopedia

import co.bdozer.utils.HashGenerator.hash
import co.bdozer.utils.HtmlToPlainText
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.http.HttpHost
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.jsoup.Jsoup
import java.net.URI

private val visited = hashSetOf<String>()
private val queue = ArrayDeque<String>()
private val entry: URI =
    URI.create(
        "https://www.investopedia.com/articles/investing/052814/these-sectors-benefit-rising-interest-rates.asp"
    )
private val httpHost = HttpHost("localhost", 9200)
private val restHighLevelClient = RestHighLevelClient(RestClient.builder(httpHost))
private val objectMapper: ObjectMapper =
    jacksonObjectMapper()
        .findAndRegisterModules()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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
        val formatter = HtmlToPlainText()
        val text = formatter.plainText(documentBody)

        val indexRequest = IndexRequest("investopedia")
        val obj = Investopedia(
            uri = url,
            text = text,
            title = title,
        )
        
        indexRequest
            .id(hash(url))
            .source(objectMapper.writeValueAsString(obj), XContentType.JSON)
        val indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT)
        
        println("Index response ${indexResponse.result} url=${url} visited=${visited.size} queue=${queue.size}")
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
