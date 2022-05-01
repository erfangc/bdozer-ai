package co.bdozer.master.models

data class EarningsGrowth(
    val threeYearsAgo: Double? = null,
    val twoYearsAgo: Double? = null,
    val lastYear: Double? = null,
    val thisYear: Double? = null,
)