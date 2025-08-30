package tech.aliorpse.mcutils.model.modrinth.search

import com.squareup.moshi.Json

data class ModrinthSearchResponse(
    val hits: List<ModrinthSearchResult>,
    @field:Json(name = "offset") val offset: Int,
    @field:Json(name = "limit") val limit: Int,
    @field:Json(name = "total_hits") val totalHits: Int
)
