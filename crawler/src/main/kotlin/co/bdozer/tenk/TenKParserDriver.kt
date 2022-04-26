package co.bdozer.tenk

import org.slf4j.LoggerFactory
import java.io.FileInputStream
import kotlin.system.exitProcess

/**
 * This program takes a list of S&P companies
 * and for each one ... queries its CIK and then ingest the latest 10-K filing
 */
fun main() {
    val log = LoggerFactory.getLogger("Driver")
    val tickers = FileInputStream("crawler/tickers.txt")
        .bufferedReader()
        .readLines()
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val tenKProcessor = TenKProcessor()
    for (ticker in tickers) {
        try {
            tenKProcessor.processTicker(ticker)
        } catch (e: Exception) {
            log.error("Exception occurred while processing ticker={}, error={}", ticker, e.message)
        }
    }
    exitProcess(0)
}

