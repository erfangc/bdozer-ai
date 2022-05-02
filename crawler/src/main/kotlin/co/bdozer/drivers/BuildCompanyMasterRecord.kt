package co.bdozer.drivers

import co.bdozer.master.CompanyMasterBuilder
import kotlin.system.exitProcess

fun main() {
    CompanyMasterBuilder.buildCompanyRecord("T")
    exitProcess(0)
}