package co.bdozer.master.models

data class PerShareMetrics(
    val basicNetEps: Double? = null,
    val dilutedNetEps: Double? = null,
    val freeCashFlowPerShare: Double? = null,
    val operCashFlowPerShare: Double? = null,
    val bookValPerShare: Double? = null,
)