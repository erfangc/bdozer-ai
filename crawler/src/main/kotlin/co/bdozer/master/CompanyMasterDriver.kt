package co.bdozer.master

import co.bdozer.master.models.CompanyMasterRecord
import co.bdozer.utils.Database.runSql
import co.bdozer.zacks.FC
import co.bdozer.zacks.MT

fun main() {
    val ticker = "TDOC"
    
    val mt = runSql("""
        select ticker, exchange, comp_name, comp_name_2, comp_cik, comp_url 
        from mt 
        where ticker = '$ticker'
    """.trimIndent(), MT::class).first()
    
    val fcs = fc(ticker)
    
    val record = CompanyMasterRecord(
        id = ticker,
        ticker = ticker,
        cik = mt.comp_cik,
        exchange = mt.exchange,
        companyName = mt.comp_name ?: mt.comp_name_2,
        companyUrl = mt.comp_url,
        business = null,
        earningsGrowth = null,
        enterpriseValue = null,
        latestMetrics = null,
        marketCap = null,
        price = null,
        perShareMetrics = null,
        riskFactors = null,
        salesGrowth = null,
    )
    
}

private fun fc(ticker: String): List<FC> {
    return runSql(
        """
        select *
        from fc
        where m_ticker = '$ticker'
        order by qtr_nbr desc 
    """.trimIndent(),
        FC::class
    ).toList()
}