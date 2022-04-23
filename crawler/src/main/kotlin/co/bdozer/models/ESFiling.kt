package co.bdozer.models

data class ESFiling(
    val cik: String,
    val ash: String,
    val url: String,
    val reportDate: String,
    val text: String,
    val section: String,
    val form: String,
    val ticker: String,
    val companyName: String,
    val timestamp: String,
)