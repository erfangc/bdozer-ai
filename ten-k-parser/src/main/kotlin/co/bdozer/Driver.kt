package co.bdozer

import java.io.FileInputStream
import kotlin.system.exitProcess

fun main() {

    val tickers = FileInputStream("ten-k-parser/tickers.txt").bufferedReader().readLines().map { it.trim() }
        .filter { it.isNotBlank() }

    for (ticker in tickers) {
        try {
            processSingleCompany(ticker)
        } catch (e: Exception) {
            log.error("Exception occurred while processing ticker={}, error={}", ticker, e.message)
        }
    }

    exitProcess(0)

}

