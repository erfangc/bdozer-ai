package co.bdozer.master

import co.bdozer.master.calculators.FCS
import co.bdozer.master.calculators.fcs
import co.bdozer.master.calculators.frs
import co.bdozer.master.calculators.price
import co.bdozer.master.models.CompanyMasterRecord
import co.bdozer.master.models.MarketData
import co.bdozer.polygon.Polygon.tickerDetailV3
import co.bdozer.polygon.models.TickerDetailV3
import co.bdozer.utils.Beans

fun main() {
    val ticker = "TDOC"
    
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
    )

    val objectWriter = Beans.objectMapper().writerWithDefaultPrettyPrinter()
    println(objectWriter.writeValueAsString(record))

}

fun enterpriseValue(tickerDetailV3: TickerDetailV3, fcs: FCS): Double {
    val totLiab = fcs.quarters.first().tot_liab
    return tickerDetailV3.results.market_cap + (totLiab ?: 0.0)
}
