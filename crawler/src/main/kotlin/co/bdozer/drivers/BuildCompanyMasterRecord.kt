package co.bdozer.drivers

import co.bdozer.master.CompanyMasterBuilder

fun main() {
    val record = CompanyMasterBuilder.buildCompanyRecord("NVAX")
    println(record)
}