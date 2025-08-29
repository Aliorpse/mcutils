package tech.aliorpse.mcutils.model.modrinth

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ModrinthSearchResponse(
    val hits: List<ModrinthProject>,
    @field:Json(name = "offset") val offset: Int,
    @field:Json(name = "limit") val limit: Int,
    @field:Json(name = "total_hits") val totalHits: Int
)
