package co.bdozer.master

data class CompanyMasterRecord(
    val id: String,
    val ticker: String,
    val companyName: String,
    val price: Double? = null,
    val marketCap: Double? = null,
    val freeCashFlowPerShare: Double? = null,
    val operCashFlowPerShare: Double? = null,
    val bookValPerShare: Double? = null,
    val totRevnu: Double? = null,
    val ebit: Double? = null,
    val ebitda: Double? = null,
    val totAsset: Double? = null,
    val totLiab: Double? = null,
    val totLtermDebt: Double? = null,
    val debtToEquity: Double? = null,
    val basicNetEps: Double? = null,
    val dilutedNetEps: Double? = null,
    val business: List<String> = emptyList(),
    val riskFactors: List<String> = emptyList(),
    val products: List<String> = emptyList(),
)
