package co.bdozer.master

import co.bdozer.master.models.CompanyMasterRecord
import co.bdozer.utils.DataAccess.runSql

fun main() {
    val ticker = "TDOC"    
    val results = runSql("select ticker, exchange, comp_name, comp_name_2, comp_cik, comp_url from mt where ticker = '$ticker'").first()
    val record = CompanyMasterRecord(
        id = ticker,
        ticker = ticker,
        cik = results["comp_cik"].toString(),
        exchange = results["exchange"].toString(),
        companyName = results["comp_name"].toString(),
        companyUrl = results["comp_url"].toString(),
    )

    println(record)
}