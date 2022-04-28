package co.bdozer.ai.server

import co.bdozer.core.nlp.sdk.api.CoreNlpApi
import co.bdozer.core.nlp.sdk.model.CrossEncodeInput
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SearchController(
    private val restHighLevelClient: RestHighLevelClient,
    private val coreNLP: CoreNlpApi,
    private val objectMapper: ObjectMapper,
) {

    private val log = LoggerFactory.getLogger(SearchController::class.java)

    @GetMapping("semantic-search")
    fun search(@RequestParam query: String): List<Result> {
        val tenKs = searchFilings(query)

        // rerank based on cross-encoder proximity for semantic closeness
        // divide into sentences ... and draw boundaries and then compute the average score
        return tenKs
            .map { tenK ->
                Result(
                    cik = tenK.cik,
                    ticker = tenK.ticker,
                    score = scoreFiling(query, tenK),
                    companyName = tenK.companyName,
                    hits = listOf(tenK.text),
                )
            }
            .groupBy { it.cik }
            .map { (cik, values) ->
                val first = values.first()
                Result(
                    cik = cik,
                    score = values.map { it.score ?: 0.0 }.average(),
                    ticker = first.ticker,
                    companyName = first.companyName,
                    hits = values.flatMap { it.hits },
                )
            }
            .sortedByDescending { it.score }

    }

    private fun scoreFiling(query: String, filing: TenK): Double {
        val chunks = filing.text.take(15000).chunked(500)
        val scoredSentences = coreNLP.crossEncode(
            CrossEncodeInput()
                .reference(query)
                .comparisons(chunks)
        )
        val averageScore = scoredSentences.maxOfOrNull { it.score.toDouble() }
        log.info(
            "Scoring filing ticker={} averageScore={}, sentences and scores as follows:", filing.ticker, averageScore
        )
        scoredSentences.forEach { scoredSentence ->
            log.info("text={}, score={}", scoredSentence.sentence, scoredSentence.score)
        }
        return averageScore ?: Double.NEGATIVE_INFINITY
    }

    private fun searchFilings(query: String): List<TenK> {
        val searchResponse = restHighLevelClient.search(searchRequest(query), RequestOptions.DEFAULT)
        val searchHits = searchResponse.hits
        val filings = searchHits.hits.map { hit ->
            objectMapper.readValue<TenK>(hit.sourceAsString)
        }
        return filings
    }

    private fun searchRequest(query: String) = SearchRequest("ten-k").source(
        SearchSourceBuilder.searchSource().query(QueryBuilders.matchQuery("text", query)).size(40)
    )
}