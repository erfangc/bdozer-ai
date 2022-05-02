package co.bdozer.master

import co.bdozer.master.calculators.fcs
import co.bdozer.master.calculators.frs
import co.bdozer.master.models.CompanyMasterRecord
import co.bdozer.utils.Beans

fun main() {
    val ticker = "TDOC"

    val mt = mt(ticker)
    val frs = frs(ticker)
    val fcs = fcs(ticker)
    val rawData = RawData(mt = mt, fcs = fcs, frs = frs)

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
        enterpriseValue = null,
        marketCap = null,
        price = null,
        perShareMetrics = perShareMetrics(rawData),
    )

    println(Beans.objectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(record))

}

