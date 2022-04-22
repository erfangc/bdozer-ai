package co.bdozer

import co.bdozer.models.ESFiling
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.http.HttpHost
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.TermQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import java.io.File
import java.io.PrintWriter
import java.net.URI
import kotlin.system.exitProcess

fun localElasticsearch(): RestHighLevelClient {
    val uri = URI.create(
        System.getenv("ELASTICSEARCH_ENDPOINT") ?: "http://localhost:9200"
    )
    val httpHost = HttpHost(uri.host, uri.port, uri.scheme)
    return RestHighLevelClient(RestClient.builder(httpHost))
}

fun objectMapper(): ObjectMapper {
    return jacksonObjectMapper()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}

const val separator =
    "------------------------------------------------------------" +
            "------------------------------------------------------------"

fun main() {
    val restHighLevelClient = localElasticsearch()
    val objectMapper = objectMapper()
    val tickers = File("ten-k-parser/tickers.txt")
        .readLines()

    for (ticker in tickers) {
        val searchRequest = SearchRequest("filings").source(
            SearchSourceBuilder
                .searchSource()
                .query(TermQueryBuilder("ticker.keyword", ticker))
        )
        val searchResponse = restHighLevelClient.search(
            searchRequest, RequestOptions.DEFAULT
        )
        val hits = searchResponse.hits.hits
        val hit = hits.firstOrNull()
        if (hit != null) {
            val esFiling = objectMapper.readValue<ESFiling>(hit.sourceAsString)
            val pages = esFiling.text.split(separator)

            val writer = PrintWriter("ten-k-parser/output/$ticker.out")
            writer.println(pages.firstOrNull())
            writer.close()
        }
    }

    exitProcess(0)
}