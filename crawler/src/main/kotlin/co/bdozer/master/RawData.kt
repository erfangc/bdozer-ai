package co.bdozer.master

import co.bdozer.zacks.models.MT

data class RawData(
    val fcs : FCS,
    val frs : FRS,
    val mt: MT,
)