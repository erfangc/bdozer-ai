package co.bdozer

import org.slf4j.LoggerFactory
import java.io.FileInputStream
import kotlin.system.exitProcess

fun main() {
    val log = LoggerFactory.getLogger("Driver")
    val tickers = FileInputStream("ten-k-parser/tickers.txt").bufferedReader().readLines().map { it.trim() }
        .filter { it.isNotBlank() }

    val tenKProcessor = TenKProcessor()
    for (ticker in tickers) {
        try {
            tenKProcessor.processSingleCompany(ticker)
        } catch (e: Exception) {
            log.error("Exception occurred while processing ticker={}, error={}", ticker, e.message)
        }
    }
    exitProcess(0)
}

