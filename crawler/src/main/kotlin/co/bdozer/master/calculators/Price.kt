package co.bdozer.master.calculators

import co.bdozer.polygon.Polygon

fun price(ticker: String): Double {
    val previousClose = Polygon.previousClose(ticker)
    return if (previousClose.resultsCount != 1) {
        error("cannot find a price for $ticker")
    } else {
        previousClose.results.first().c
    }
}