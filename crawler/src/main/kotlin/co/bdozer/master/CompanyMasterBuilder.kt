package co.bdozer.master

import co.bdozer.master.calculators.*
import co.bdozer.master.models.AnswersFromTenKs
import co.bdozer.master.models.CompanyMasterRecord
import co.bdozer.master.models.MarketData
import co.bdozer.polygon.Polygon.tickerDetailV3
import co.bdozer.polygon.models.TickerDetailV3
import co.bdozer.tenk.TenKProcessor
import co.bdozer.tenk.models.TenK
import co.bdozer.utils.Beans
import com.fasterxml.jackson.databind.ObjectWriter
import org.slf4j.LoggerFactory

object CompanyMasterBuilder {

    private val log = LoggerFactory.getLogger(CompanyMasterBuilder::class.java)
    private val objectWriter: ObjectWriter = Beans.objectMapper().writerWithDefaultPrettyPrinter()
    fun buildCompanyRecord(ticker: String): CompanyMasterRecord {
        val tickerDetailV3 = tickerDetailV3(ticker)
        val marketCap = tickerDetailV3.results.market_cap

        val price = price(ticker)
        val mt = mt(ticker)
        val frs = frs(ticker)
        val fcs = fcs(ticker)

        val enterpriseValue = enterpriseValue(tickerDetailV3, fcs)
        val rawData = RawData(
            mt = mt,
            fcs = fcs,
            frs = frs,
            marketData = MarketData(
                enterpriseValue = enterpriseValue,
                price = price,
                marketCap = marketCap,
            ),
        )

        val record = CompanyMasterRecord(
            id = ticker,
            ticker = ticker,
            cik = mt.comp_cik,
            exchange = mt.exchange,
            companyName = mt.comp_name ?: mt.comp_name_2,
            companyUrl = mt.comp_url,
            earnings = trend(fcs.quarters) { it.net_income_loss },
            sales = trend(fcs.quarters) { it.tot_revnu },
            latestMetrics = latestMetrics(rawData),
            enterpriseValue = enterpriseValue,
            marketCap = marketCap,
            price = price,
            perShareMetrics = perShareMetrics(rawData),
            answersFromTenKs = null,
        )

        /*
        See what else we can learn from the company's TenKs
         */
        val record1 = addAnswersFromTenK(ticker, record)
        log.info(objectWriter.writeValueAsString(record1))

        return record1
    }

    private fun addAnswersFromTenK(
        ticker: String,
        record: CompanyMasterRecord
    ): CompanyMasterRecord {
        val tenKs = parseTenK(ticker)
        val record1 = if (tenKs.isNotEmpty()) {
            val first = tenKs.first()
            val ash = first.ash
            val url = first.url
            val reportDate = first.reportDate

            tenKs.map { tenK -> tenK.text }

            val questions = listOf(
                "What products do we produce?",
                "What products does the company produce?",
                "What products do we offer?",
                "What products does the company offer?",
                "Whom do we serve?",
                "Whom does the company serve?",
                "What is our business model?",
                "What is the company's business model?",
                "What is our value proposition to customers?",
                "What is the company's value proposition to customers?",
            )

            /*
            For every question conduct a semantic search based on the tenK results
             */
            val answers = questions.map { question ->
                QuestionAnswerMachine.answerQuestion(question, tenKs)
            }
            record.copy(
                answersFromTenKs = AnswersFromTenKs(
                    url = url,
                    reportDate = reportDate,
                    ash = ash,
                    answers = answers,
                )
            )
        } else {
            record
        }
        return record1
    }

    private fun parseTenK(ticker: String): List<TenK> {
        return TenKProcessor.processTicker(ticker = ticker)
    }

    private fun enterpriseValue(tickerDetailV3: TickerDetailV3, fcs: FCS): Double {
        val totLiab = fcs.quarters.first().tot_liab
        return tickerDetailV3.results.market_cap + (totLiab ?: 0.0)
    }

}
