package co.bdozer.ai.server.services.tenkparser

import co.bdozer.ai.server.services.tenkparser.models.Text
import co.bdozer.ai.server.services.tenkparser.models.Topic
import co.bdozer.core.nlp.sdk.ApiClient
import co.bdozer.core.nlp.sdk.api.DefaultApi
import co.bdozer.core.nlp.sdk.model.CrossEncodeInput
import co.bdozer.core.nlp.sdk.model.DocInput
import co.bdozer.core.nlp.sdk.model.ZeroShotClassificationRequest
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.commons.codec.digest.DigestUtils
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
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
    private final val objectMapper = jacksonObjectMapper().findAndRegisterModules()
    private final val log = LoggerFactory.getLogger(TenKParser::class.java)

    init {
        apiClient.basePath = System.getenv("CORE_NLP_ENDPOINT") ?: "http://localhost:8000"
    }

    private val coreNlp = apiClient.buildClient(DefaultApi::class.java)

    fun parse10k(cik: String, ash: String) {
        var totalSentences = 0

        val original10K = original10K(cik, ash)
        val docUrl =
            "$secEndpoint/Archives/edgar/data/$cik/${ash.padStart(length = 18, padChar = '0')}/$original10K"
        log.info(
            "Retrieving document from docUrl={}, cik={}, ash={}",
            docUrl,
            cik,
            ash,
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

        val tenK = Jsoup.connect(docUrl).get()
        val spans = spans(tenK)

        val start = System.currentTimeMillis()
        log.info("Calling NLP server with spans size={}", spans.size)
        val sentences = spans.flatMapIndexed { idx, span ->
            log.info("Calling sentence_producer idx={}, totalSentences={}", idx, totalSentences)
            val sentences = coreNlp.getSentences(DocInput().doc(span)).sentences.filterNotNull()
            log.info(
                "Calling classification and cross encoder, sentences.size={}, totalSentences={}",
                sentences.size,
                totalSentences
            )
            val texts = sentences.map { toText(sentence = it, meta = meta) }
            index(texts)
            totalSentences += sentences.size
            sentences
        }
        val stop = System.currentTimeMillis()
        log.info(
            "NLP server returned {} sentences in {}s",
            sentences.size,
            TimeUnit.SECONDS.convert(stop - start, TimeUnit.MILLISECONDS),
        )
    }

    private fun index(texts: List<Text>) {
        val bulkRequest = BulkRequest()
        for (text in texts) {
            val indexRequest = IndexRequest("texts")
            val json = objectMapper.writeValueAsString(text)
            indexRequest.source(json, XContentType.JSON)
            bulkRequest.add(indexRequest)
        }
        val took = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT).took
        log.info("Indexing texts texts.size={}, took={}, total={}", texts.size, took)
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

        val crossEncodeInput = CrossEncodeInput()
            .reference(sentence)
            .comparisons(
                listOf(
                    competitorQuestion,
                    riskFactorQuestion,
                    productQuestion1,
                    productQuestion2,
                    productQuestion3,
                    costQuestion,
                )
            )

        val scoredSentences = coreNlp.crossEncode(crossEncodeInput)

        val competitorScore = scoredSentences.find { it.sentence == competitorQuestion }?.score?.toDouble()
        val productScore1 = scoredSentences.find { it.sentence == productQuestion1 }?.score?.toDouble()
        val productScore2 = scoredSentences.find { it.sentence == productQuestion2 }?.score?.toDouble()
        val productScore3 = scoredSentences.find { it.sentence == productQuestion3 }?.score?.toDouble()
        val riskFactorScore = scoredSentences.find { it.sentence == riskFactorQuestion }?.score?.toDouble()
        val costScore = scoredSentences.find { it.sentence == costQuestion }?.score?.toDouble()

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
            competitorScore = competitorScore,
            productScore1 = productScore1,
            productScore2 = productScore2,
            productScore3 = productScore3,
            costScore = costScore,
            riskFactorScore = riskFactorScore,
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