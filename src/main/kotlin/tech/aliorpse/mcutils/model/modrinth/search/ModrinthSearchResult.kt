package tech.aliorpse.mcutils.model.modrinth.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ModrinthSearchResult(
    val slug: String,
    val title: String,
    val description: String,
    val categories: List<String>,
    @SerialName("client_side") val clientSide: String,
    @SerialName("server_side") val serverSide: String,
    @SerialName("project_type") val projectType: String,
    val downloads: Int,
    @SerialName("icon_url") val iconUrl: String? = null,
    val color: Int? = null,
    @SerialName("thread_id") val threadId: String? = null,
    @SerialName("monetization_status") val monetizationStatus: String? = null,
    @SerialName("project_id") val projectId: String,
    val author: String,
    @SerialName("display_categories") val displayCategories: List<String>? = null,
    val versions: List<String>,
    val follows: Int,
    @SerialName("date_created") val dateCreated: String,
    @SerialName("date_modified") val dateModified: String,
    @SerialName("latest_version") val latestVersion: String? = null,
    val license: String,
    val gallery: List<String>? = null,
    @SerialName("featured_gallery") val featuredGallery: String? = null
)
