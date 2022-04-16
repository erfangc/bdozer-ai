package co.bdozer.rss.models

import java.time.Instant

data class Entry(
    val title: String?,
    val id: String,
    val link: String?,
    val updated: Instant?,
    val summary: String?,
    val category: Category?,
    val cik: String?,
    val original10K: String?,
    val ash: String?,
)