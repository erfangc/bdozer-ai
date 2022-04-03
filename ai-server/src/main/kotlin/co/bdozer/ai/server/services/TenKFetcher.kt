package co.bdozer.ai.server.services

import co.bdozer.core.nlp.sdk.ApiClient
import co.bdozer.core.nlp.sdk.api.DefaultApi
import co.bdozer.core.nlp.sdk.model.ZeroShotClassificationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object TenKFetcher {
    val secEndpoint = "https://www.sec.gov"

    fun original10K(cik: String, ash: String): String {
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

fun main() {
    val api = ApiClient().setBasePath("http://localhost:8000").buildClient(DefaultApi::class.java)

    /**
     * Returns the overall score of how likely this is the table of content
     */
    fun scoreTable(table: Element): Double {
        val expectedRows = listOf(
            "Item Business",
            "Item Risk Factors",
            "Item Unresolved Staff Comments",
        )
        // find max score for each expected row them sum them to 
        // determine the relevance of the entire table
        return expectedRows.sumOf { expectedRow ->
            val tableRows = table.select("tr").map { it.text() }
            val resposne = api.zeroShotClassificationZeroShotClassificationPost(
                ZeroShotClassificationRequest()
                    .sentence(expectedRow)
                    .candidateLabels(tableRows)
            )
            resposne.result.maxOf { it.score.toDouble() }
        }

    }

    fun getAnchor(table: Element?, subject: String): String? {
        println("Finding anchor tag for subject=$subject")
        val rows = table?.select("tr")
        val business = rows?.map {
            val response = api.zeroShotClassificationZeroShotClassificationPost(
                ZeroShotClassificationRequest()
                    .sentence(subject)
                    .candidateLabels(listOf(it.text()))
            )
            it to response.result.first().score
        }?.maxByOrNull { it.second }?.first
        return business?.select("a")?.attr("href")
    }

    val cik = "1839132"
    val ash = "000121390022017432"
    val original10K = TenKFetcher.original10K(cik, ash)
    val url =
        "${TenKFetcher.secEndpoint}/Archives/edgar/data/${cik}/${ash.padStart(length = 18, padChar = '0')}/$original10K"
    println(url)

    val doc = Jsoup.connect(url).get()

    val tables = doc.select("table").take(10)
    val tableOfContent = tables.maxByOrNull { scoreTable(it) }
    val businessAnchor = getAnchor(tableOfContent, "Item Business")
    val riskAnchor = getAnchor(tableOfContent, "Risk Factors")

    println(businessAnchor)
    println(riskAnchor)
}