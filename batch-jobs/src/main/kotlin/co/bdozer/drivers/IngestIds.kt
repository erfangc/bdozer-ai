package co.bdozer.drivers

import co.bdozer.tenk.IdsIngestor
import kotlin.system.exitProcess

fun main() {
    IdsIngestor.ingestIds()
    exitProcess(0)
}