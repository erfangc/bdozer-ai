package co.bdozer.master

import co.bdozer.master.TrendFn.trend
import co.bdozer.master.models.CompanyMasterRecord
import co.bdozer.master.models.LatestMetrics
import co.bdozer.master.models.PerShareMetrics
import co.bdozer.utils.Beans
import co.bdozer.utils.Database.runSql
import co.bdozer.zacks.models.FC
import co.bdozer.zacks.models.FR
import co.bdozer.zacks.models.MT
import org.intellij.lang.annotations.Language

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
        business = null,
        earnings = trend(fcs.quarters) { it.net_income_loss },
        sales = trend(fcs.quarters) { it.tot_revnu },
        enterpriseValue = null,
        latestMetrics = latestMetrics(rawData),
        marketCap = null,
        price = null,
        perShareMetrics = perShareMetrics(rawData),
        riskFactors = null,
    )

    println(Beans.objectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(record))

}

fun perShareMetrics(rawData: RawData): PerShareMetrics {
    val fr = rawData.frs.quarters.last()
    println("FR: ${fr.per_end_date} ${fr.per_type}")
    return PerShareMetrics(
        basicNetEps = 0.0,
        dilutedNetEps = 0.0,
        freeCashFlowPerShare = fr.free_cash_flow_per_share,
        operCashFlowPerShare = fr.oper_cash_flow_per_share,
        bookValPerShare = fr.book_val_per_share,
    )
}

fun mt(ticker: String): MT {
    return runSql(
        sql = """
        select * 
        from mt 
        where ticker = '$ticker'
    """.trimIndent(), MT::class
    ).first()
}

fun fcs(ticker: String): FCS {
    @Language("PostgreSQL") val results = runSql(
        sql = """
            select *
            from fc
            where ticker = '$ticker' 
            order by qtr_nbr desc 
            """.trimIndent(), FC::class
    ).toList()

    val groupBy = results.groupBy { it.per_type }
    val annuals = groupBy["A"]?.take(2) ?: emptyList()
    val quarters = groupBy["Q"]?.take(5) ?: emptyList()

    return FCS(annuals = annuals, quarters = quarters)
}

fun frs(ticker:String): FRS {
    @Language("PostgreSQL") val results = runSql(
        sql = """
                select *
                from fr
                where ticker = '$ticker'
                order by per_end_date desc
                """.trimIndent(),
        FR::class,
    ).toList()
    val groupBy = results.groupBy { it.per_type }
    val annuals = groupBy["A"]?.take(2) ?: emptyList()
    val quarters = groupBy["Q"]?.take(5) ?: emptyList()
    return FRS(annuals = annuals, quarters = quarters)
}

fun latestMetrics(rawData: RawData): LatestMetrics {
    val (fcs, _, _) = rawData
    
    val revenue = fcs.quarters.sumOf { it.tot_revnu ?: 0.0 }
    val ebit = fcs.quarters.sumOf { it.ebit ?: 0.0 }
    val ebitda = fcs.quarters.sumOf { it.ebitda ?: 0.0 }
    val netIncome = fcs.quarters.sumOf { it.net_income_loss ?: 0.0 }

    val latestQ = fcs.quarters.first()
    val latestA = fcs.annuals.first()

    val latest = if (latestQ.per_end_date?.isAfter(latestA.per_end_date) == true) {
        latestQ
    } else {
        latestA
    }

    val equity = latest.tot_share_holder_equity?: 0.0
    val debt = latest.tot_liab?: 0.0
    val ltDebt = latest.tot_lterm_debt ?: 0.0
    val asset = latest.tot_asset ?: 0.0

    return LatestMetrics(
        revenue = revenue,
        ebitda = ebitda,
        ebit = ebit,
        netIncome = netIncome,
        debtToEquity = debt / equity,
        debtToAsset = debt / asset,
        totalAsset = asset,
        totalLiability = debt,
        longTermDebt = ltDebt,
        longTermDebtToAsset = ltDebt / asset,
    )
}