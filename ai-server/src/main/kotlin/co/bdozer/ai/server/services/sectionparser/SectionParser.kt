package co.bdozer.ai.server.services.sectionparser

import co.bdozer.core.nlp.sdk.ApiClient
import co.bdozer.core.nlp.sdk.api.DefaultApi
import co.bdozer.core.nlp.sdk.model.ZeroShotClassificationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SectionParser {

    private val secEndpoint = "https://www.sec.gov"
    private val log = LoggerFactory.getLogger(SectionParser::class.java)
    private final val apiClient = ApiClient()

    init {
        apiClient.basePath = System.getenv("CORE_NLP_ENDPOINT") ?: "http://localhost:8000"
    }

    private val coreNlp = apiClient.buildClient(DefaultApi::class.java)

    fun findSections(cik: String, ash: String): TenKSections {

        val original10K = original10K(cik, ash)
        val url =
            "${secEndpoint}/Archives/edgar/data/${cik}/${ash.padStart(length = 18, padChar = '0')}/$original10K"
        println(url)

        val doc = Jsoup.connect(url).get()

        val tables = doc.select("table").take(10)
        val tableOfContent = tables.maxByOrNull { scoreTable(it) }
        val business = tableRowToSection(tableOfContent, "Item Business")
        val riskFactors = tableRowToSection(tableOfContent, "Risk Factors")

        println(business)
        println(riskFactors)

        return TenKSections(
            business = business,
            riskFactors = riskFactors,
        )
    }

    private fun tableRowToSection(table: Element?, subject: String): Section {

        log.info("Finding anchor tag for subject=$subject")

        val rows = table?.select("tr")
        val tgtRow = rows?.map {
            val response = coreNlp.zeroShotClassification(
                ZeroShotClassificationRequest()
                    .sentence(subject)
                    .candidateLabels(listOf(it.text()))
            )
            it to response.result.first().score
        }?.maxByOrNull { it.second }?.first

        var idx = (rows?.indexOf(tgtRow) ?: 0) + 1
        fun hasHref(idx: Int): String? {
            val href = rows
                ?.get(idx)
                ?.select("a")
                ?.attr("href")
            return if (href?.isBlank() == true) {
                null
            } else {
                href
            }
        }

        val size = rows?.size ?: 0
        var endAnchor: String? = null
        while (idx < size) {
            endAnchor = hasHref(idx)
            if (endAnchor != null) {
                break
            } else {
                idx++
            }
        }

        val startAnchor = tgtRow?.select("a")?.attr("href")
        return Section(startAnchor = startAnchor, endAnchor = endAnchor)
    }

    /**
     * Returns the overall score of how likely this is the table of content
     */
    private fun scoreTable(table: Element): Double {
        val expectedRows = listOf(
            "Item Business",
            "Item Risk Factors",
            "Item Unresolved Staff Comments",
        )
        // find max score for each expected row them sum them to 
        // determine the relevance of the entire table
        return expectedRows.sumOf { expectedRow ->
            val tableRows = table.select("tr").map { it.text() }
            val response = coreNlp.zeroShotClassification(
                ZeroShotClassificationRequest()
                    .sentence(expectedRow)
                    .candidateLabels(tableRows)
            )
            response.result.maxOf { it.score.toDouble() }
        }

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