package co.bdozer.ai.server.services.tenkparser.models

import java.time.Instant
import java.time.LocalDate

data class Text(
    /*
    Generated from SHA-256 hash of the content of the text, and it's docUrl
     */
    val textId: String,
    val type: String,
    val source: String,
    val cik: String,
    val ash: String,
    val lastUpdated: Instant,
    val docUrl: String,
    val sentence: String,
    val namedEntities: List<NamedEntity> = emptyList(),
    val productScore1: Double? = null,
    val productScore2: Double? = null,
    val productScore3: Double? = null,
    val competitorScore: Double? = null,
    val riskFactorScore: Double? = null,
    val costScore: Double?,
    val topics: List<Topic> = emptyList(),
)

