package tech.aliorpse.mcutils.model.modrinth.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ModrinthSearchResponse(
    val hits: List<ModrinthSearchResult>,
    val offset: Int,
    val limit: Int,
    @SerialName("total_hits") val totalHits: Int
)
