package co.bdozer.master

import co.bdozer.utils.Database
import co.bdozer.zacks.models.MT
import org.intellij.lang.annotations.Language

fun mt(ticker: String): MT {
    @Language("PostgreSQL") val result = Database.runSql(
        sql = """
        select * 
        from mt 
        where ticker = '$ticker'
    """.trimIndent(), MT::class
    ).first()
    
    println("Loaded master table for $ticker")
    return result
}