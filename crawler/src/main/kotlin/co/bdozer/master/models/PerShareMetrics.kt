package co.bdozer.master.models

data class PerShareMetrics(
    val epsBasicNet: Double?,
    val epsBasicContOper: Double?,
    val epsBasicDiscontOper: Double?,
    val epsBasicExtra: Double?,
    val epsDilutedNet: Double?,
    val epsDilutedContOper: Double?,
    val epsDilutedDiscontOper: Double?,
    val epsDilutedExtra: Double?,
    val freeCashFlowPerShare: Double?,
    val operCashFlowPerShare: Double?,
    val bookValPerShare: Double?,
)