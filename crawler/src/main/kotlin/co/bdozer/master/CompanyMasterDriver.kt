package co.bdozer.master

import co.bdozer.utils.DataAccess.query

fun main() {
    val results = query(
        sql = """
        select
            m_ticker,
            per_end_date,
            net_income_loss,
            diluted_net_eps
        from
            fc
        where
            m_ticker = 'BLK' and per_type = 'A'
        order by 
            per_end_date desc
    """.trimIndent()
    )
    println(results.toList())
}