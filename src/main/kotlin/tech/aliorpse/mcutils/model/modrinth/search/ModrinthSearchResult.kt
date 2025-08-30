package tech.aliorpse.mcutils.model.modrinth.search

import com.squareup.moshi.Json

data class ModrinthSearchResult(
    val slug: String,
    val title: String,
    val description: String,
    val categories: List<String>,
    @field:Json(name = "client_side") val clientSide: String,
    @field:Json(name = "server_side") val serverSide: String,
    @field:Json(name = "project_type") val projectType: String,
    val downloads: Int,
    @field:Json(name = "icon_url") val iconUrl: String?,
    val color: Int?,
    @field:Json(name = "thread_id") val threadId: String?,
    @field:Json(name = "monetization_status") val monetizationStatus: String?,
    @field:Json(name = "project_id") val projectId: String,
    val author: String,
    @field:Json(name = "display_categories") val displayCategories: List<String>?,
    val versions: List<String>,
    val follows: Int,
    @field:Json(name = "date_created") val dateCreated: String,
    @field:Json(name = "date_modified") val dateModified: String,
    @field:Json(name = "latest_version") val latestVersion: String?,
    val license: String,
    val gallery: List<String>?,
    @field:Json(name = "featured_gallery") val featuredGallery: String?
)
