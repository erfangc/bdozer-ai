package co.bdozer.ai.server.services.tenkparser

import co.bdozer.ai.server.services.tenkparser.models.Text
import co.bdozer.ai.server.services.tenkparser.models.Topic
import co.bdozer.core.nlp.sdk.ApiClient
import co.bdozer.core.nlp.sdk.api.DefaultApi
import co.bdozer.core.nlp.sdk.model.DocInput
import co.bdozer.core.nlp.sdk.model.ZeroShotClassificationRequest
import org.apache.commons.codec.digest.DigestUtils
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@Service
class TenKParser(
    private val restHighLevelClient: RestHighLevelClient,
) {

    private final val secEndpoint = "https://www.sec.gov"
    private final val apiClient = ApiClient()
    private final val log = LoggerFactory.getLogger(TenKParser::class.java)

    init {
        apiClient.basePath = System.getenv("CORE_NLP_ENDPOINT") ?: "http://localhost:8000"
    }

    private val coreNlp = apiClient.buildClient(DefaultApi::class.java)

    fun parseTenK(cik: String, ash: String) {

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
            coreNlp.getSentences(DocInput().doc(span)).sentences
        }.filterNotNull()
        val stop = System.currentTimeMillis()
        log.info(
            "NLP server returned {} sentences in {}s",
            sentences.size,
            TimeUnit.SECONDS.convert(stop - start, TimeUnit.MILLISECONDS),
        )

        /*
        For each sentence - turn them into a `Text` instance and populate all the fields by calling core-nlp
         */
        val meta =
            DocMeta(
                cik = cik,
                ash = ash,
                docUrl = docUrl,
                asOfDate = asOfDate(docUrl),
                lastUpdated = Instant.now(),
            )
        val texts = sentences.map { toText(sentence = it, meta = meta) }
        index(texts)
    }

    private fun index(texts: List<Text>) {
        val bulkRequest = BulkRequest()
        for (text in texts) {
            val indexRequest = IndexRequest("texts")
            indexRequest.source(text)
            bulkRequest.add(indexRequest)
        }
        val took = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT).took
        log.info("Indexing {} texts took {}", texts.size, took)
    }

    // TODO FIX THIS
    private fun asOfDate(docUrl: String): LocalDate {
        return LocalDate.now()
    }

    data class DocMeta(
        val cik: String,
        val ash: String,
        val docUrl: String,
        val asOfDate: LocalDate,
        val lastUpdated: Instant,
    )

    private fun toText(sentence: String, meta: DocMeta): Text {

        val competitorQuestion = "Who are our competitors?"
        val riskFactorQuestion = "What are our biggest risks?"
        val productQuestion1 = "What products do we produce?"
        val productQuestion2 = "What products or services do we provide?"
        val productQuestion3 = "What do we sell?"
        val costQuestion = "What are our biggest costs?"

        return Text(
            textId = generateId(sentence, meta),
            type = "10-K",
            source = secEndpoint,
            cik = meta.cik,
            ash = meta.ash,
            asOfDate = meta.asOfDate,
            lastUpdated = meta.lastUpdated,
            docUrl = meta.docUrl,
            sentence = sentence,
            namedEntities = emptyList(),
            hasCompetitorScore = 0.0,
            hasProductScore = 0.0,
            hasCostScore = 0.0,
            hasRiskFactorScore = 0.0,
            topics = topics(sentence)
        )
    }

    private fun generateId(sentence: String, meta: DocMeta) =
        DigestUtils.sha256Hex(meta.docUrl + sentence)

    private fun topics(sentence: String): List<Topic> {
        val response = coreNlp.zeroShotClassificationZeroShotClassificationPost(
            ZeroShotClassificationRequest()
                .sentence(sentence)
                .candidateLabels(
                    listOf(
                        "Inflation",
                        "COVID-19",
                        "Gas Prices",
                        "Russian Ukraine War",
                        "Politics",
                        "Election",
                    )
                )
        )
        return response
            .result
            .toList()
            .sortedByDescending { it.score }
            .take(5)
            .map { Topic(name = it.label, score = it.score.toDouble()) }
    }

    private fun spans(doc: Document): List<String> {
        return doc.root()
            .select("span")
            .filter {
                val isNotEmpty = it.text().isNotEmpty()
                val tokens = it.text().split(" ")
                isNotEmpty && tokens.size > 10
            }
            .map { it.text() }
    }

    private fun original10K(cik: String, ash: String): String {
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

}