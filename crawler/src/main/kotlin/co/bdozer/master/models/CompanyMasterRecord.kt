package co.bdozer.master.models

data class CompanyMasterRecord(
    val id: String,
    val ticker: String,
    val cik: String?=null,
    val exchange: String? = null,
    val companyUrl: String? = null,
    val companyName: String? = null,
    val price: Double? = null,
    val marketCap: Double? = null,
    val enterpriseValue: Double? = null,
    val perShareMetrics: PerShareMetrics? = null,
    val totRevnu: Double? = null,
    val ebit: Double? = null,
    val ebitda: Double? = null,
    val totAsset: Double? = null,
    val totLiab: Double? = null,
    val totLtermDebt: Double? = null,
    val debtToEquity: Double? = null,
    val salesGrowth: SalesGrowth? = null,
    val earningsGrowth: EarningsGrowth? = null,
    val business: List<String> = emptyList(),
    val riskFactors: List<String> = emptyList(),
)

