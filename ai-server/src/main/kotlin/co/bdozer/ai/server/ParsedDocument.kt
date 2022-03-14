package co.bdozer.ai.server

data class ParsedDocument(
    val cik: String,
    val ash: String,
    val docUrl: String,
    val lines: List<String>,
)