package co.bdozer.master

import co.bdozer.master.models.PerShareMetrics

fun perShareMetrics(rawData: RawData): PerShareMetrics {
    val fr = rawData.frs.quarters.last()
    val fc = rawData.fcs.quarters.last()
    
    return PerShareMetrics(
        epsBasicNet = fc.eps_basic_net,
        epsBasicContOper = fc.eps_basic_cont_oper,
        epsBasicDiscontOper = fc.eps_basic_discont_oper,
        epsBasicExtra = fc.eps_basic_extra,
        epsDilutedNet = fc.eps_diluted_net,
        epsDilutedContOper = fc.eps_diluted_cont_oper,
        epsDilutedDiscontOper = fc.eps_diluted_discont_oper,
        epsDilutedExtra = fc.eps_diluted_extra,
        freeCashFlowPerShare = fr.free_cash_flow_per_share,
        operCashFlowPerShare = fr.oper_cash_flow_per_share,
        bookValPerShare = fr.book_val_per_share,
    )
}