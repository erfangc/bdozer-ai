package co.bdozer.master

import co.bdozer.master.calculators.FCS
import co.bdozer.master.calculators.FRS
import co.bdozer.master.models.MarketData
import co.bdozer.zacks.models.MT

data class RawData(
    val fcs : FCS,
    val frs : FRS,
    val mt: MT,
    val marketData: MarketData,
)
