package co.bdozer.master.calculators

import co.bdozer.utils.Database
import co.bdozer.zacks.models.FR
import org.intellij.lang.annotations.Language

data class FRS(
    val annuals: List<FR>,
    val quarters: List<FR>,
)

fun frs(ticker: String): FRS {
    @Language("PostgreSQL") val results = Database.runSql(
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

    println("Loaded fundamental ratios for $ticker")
    return FRS(annuals = annuals, quarters = quarters)
}