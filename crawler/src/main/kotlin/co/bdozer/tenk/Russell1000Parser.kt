package co.bdozer.tenk

import co.bdozer.tenk.TenKProcessor.processTicker
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import kotlin.system.exitProcess

/**
 * This program takes a list of S&P companies
 * and for each one ... queries its CIK and then ingest the latest 10-K filing
 */
object Russell1000Parser {
    
    private val log = LoggerFactory.getLogger(Russell1000Parser::class.java)
    fun parseRussell1000() {

        val tickers = FileInputStream("crawler/russell-1000-constituents.txt")
            .bufferedReader()
            .readLines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        var remaining = tickers.size
        var count = 0
        var error = 0
        var success = 0

        for (ticker in tickers) {
            try {
                log.info("Processing state error=$error count=$count success=$success remaining=$remaining ticker=$ticker")
                remaining--
                count++
                processTicker(ticker)
                success++
            } catch (e: Exception) {
                log.error("Exception occurred while processing ticker={}, error={}", ticker, e.message)
                error++
            }
        }
        exitProcess(0)
    }
    
}