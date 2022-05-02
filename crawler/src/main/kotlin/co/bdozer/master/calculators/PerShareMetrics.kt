package co.bdozer.master.calculators

import co.bdozer.master.RawData
import co.bdozer.master.models.PerShareMetrics

fun perShareMetrics(rawData: RawData): PerShareMetrics {
    val fr = rawData.frs.quarters.last()
    val fc = rawData.fcs.quarters.last()
    val price = rawData.marketData.price
    
    fun priceTo(denom: Double?): Double? {
        return if (denom == null || denom == 0.0) {
            null
        } else if (denom < 0) {
            null
        } else {
            price / denom
        }
    }
    
    return PerShareMetrics(
        epsBasicNet = fc.eps_basic_net,
        epsBasicContOper = fc.eps_basic_cont_oper,
        epsBasicDiscontOper = fc.eps_basic_discont_oper,
        epsBasicExtra = fc.eps_basic_extra,
        
        priceToEpsBasicNet = priceTo(fc.eps_basic_net),
        priceToEpsBasicContOper = priceTo(fc.eps_basic_cont_oper),
        priceToEpsBasicDiscontOper = priceTo(fc.eps_basic_discont_oper),
        priceToEpsBasicExtra = priceTo(fc.eps_basic_extra),
        
        epsDilutedNet = fc.eps_diluted_net,
        epsDilutedContOper = fc.eps_diluted_cont_oper,
        epsDilutedDiscontOper = fc.eps_diluted_discont_oper,
        epsDilutedExtra = fc.eps_diluted_extra,
        
        priceToEpsDilutedNet = priceTo(fc.eps_diluted_net),
        priceToEpsDilutedContOper = priceTo(fc.eps_diluted_cont_oper),
        priceToEpsDilutedDiscontOper = priceTo(fc.eps_diluted_discont_oper),
        priceToEpsDilutedExtra = priceTo(fc.eps_diluted_extra),
        
        freeCashFlowPerShare = fr.free_cash_flow_per_share,
        priceToFreeCashFlowPerShare = priceTo(fr.free_cash_flow_per_share),
        
        operCashFlowPerShare = fr.oper_cash_flow_per_share,
        priceToOperCashFlowPerShare = priceTo(fr.oper_cash_flow_per_share),
        
        bookValPerShare = fr.book_val_per_share,
        priceToBookValPerShare = priceTo(fr.book_val_per_share),
    )
}
