package tech.aliorpse.mcutils.model.modrinth.project

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tech.aliorpse.mcutils.model.modrinth.MonetizationStatus
import tech.aliorpse.mcutils.model.modrinth.ProjectStatus
import tech.aliorpse.mcutils.model.modrinth.ProjectType
import tech.aliorpse.mcutils.model.modrinth.RequestedStatus
import tech.aliorpse.mcutils.model.modrinth.SideSupport

@JsonClass(generateAdapter = true)
data class ModrinthProject(
    val slug: String,
    val title: String,
    val description: String,
    val categories: List<String>,
    @field:Json(name = "client_side") val clientSide: SideSupport,
    @field:Json(name = "server_side") val serverSide: SideSupport,
    @field:Json(name = "project_type") val projectType: ProjectType,
    val downloads: Int,
    @field:Json(name = "icon_url") val iconUrl: String?,
    val color: Int?,
    @field:Json(name = "thread_id") val threadId: String?,
    @field:Json(name = "monetization_status") val monetizationStatus: MonetizationStatus?,
    @field:Json(name = "id") val projectId: String,
    val team: String,
    val body: String?,
    val status: ProjectStatus,
    @field:Json(name = "requested_status") val requestedStatus: RequestedStatus?,
    @field:Json(name = "additional_categories") val additionalCategories: List<String>?,
    @field:Json(name = "issues_url") val issuesUrl: String?,
    @field:Json(name = "source_url") val sourceUrl: String?,
    @field:Json(name = "wiki_url") val wikiUrl: String?,
    @field:Json(name = "discord_url") val discordUrl: String?,
    @field:Json(name = "donation_urls") val donationUrls: List<DonationUrl>?,
    @field:Json(name = "body_url") val bodyUrl: String?,
    @field:Json(name = "moderator_message") val moderatorMessage: String?,
    val published: String,
    val updated: String,
    val approved: String?,
    val queued: String?,
    @field:Json(name = "followers") val followers: Int,
    val license: ProjectLicense,
    val versions: List<String>,
    @field:Json(name = "game_versions") val gameVersions: List<String>,
    val loaders: List<String>,
    val gallery: List<GalleryItem>?,
    @field:Json(name = "featured_gallery") val featuredGallery: String?
)

@JsonClass(generateAdapter = true)
data class DonationUrl(
    val id: String?,
    val platform: String?,
    val url: String
)

@JsonClass(generateAdapter = true)
data class GalleryItem(
    val url: String,
    val featured: Boolean,
    val title: String?,
    val description: String?,
    val created: String
)

@JsonClass(generateAdapter = true)
data class ProjectLicense(
    val id: String,
    val name: String?,
    val url: String?
)

