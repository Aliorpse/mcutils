package tech.aliorpse.mcutils.model.modrinth.project

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.aliorpse.mcutils.model.modrinth.MonetizationStatus
import tech.aliorpse.mcutils.model.modrinth.ProjectStatus
import tech.aliorpse.mcutils.model.modrinth.ProjectType
import tech.aliorpse.mcutils.model.modrinth.RequestedStatus
import tech.aliorpse.mcutils.model.modrinth.SideSupport

@Serializable
public data class ModrinthProject(
    val slug: String,
    val title: String,
    val description: String,
    val categories: List<String>,
    @SerialName("client_side") val clientSide: SideSupport,
    @SerialName("server_side") val serverSide: SideSupport,
    @SerialName("project_type") val projectType: ProjectType,
    val downloads: Int,
    @SerialName("icon_url") val iconUrl: String? = null,
    val color: Int? = null,
    @SerialName("thread_id") val threadId: String? = null,
    @SerialName("monetization_status") val monetizationStatus: MonetizationStatus? = null,
    @SerialName("id") val projectId: String,
    val team: String,
    val body: String? = null,
    val status: ProjectStatus,
    @SerialName("requested_status") val requestedStatus: RequestedStatus? = null,
    @SerialName("additional_categories") val additionalCategories: List<String>? = null,
    @SerialName("issues_url") val issuesUrl: String? = null,
    @SerialName("source_url") val sourceUrl: String? = null,
    @SerialName("wiki_url") val wikiUrl: String? = null,
    @SerialName("discord_url") val discordUrl: String? = null,
    @SerialName("donation_urls") val donationUrls: List<DonationUrl>? = null,
    @SerialName("body_url") val bodyUrl: String? = null,
    @SerialName("moderator_message") val moderatorMessage: String? = null,
    val published: String,
    val updated: String,
    val approved: String? = null,
    val queued: String? = null,
    val followers: Int,
    val license: ProjectLicense,
    val versions: List<String>,
    @SerialName("game_versions") val gameVersions: List<String>,
    val loaders: List<String>,
    val gallery: List<GalleryItem>? = null,
    @SerialName("featured_gallery") val featuredGallery: String? = null
)

@Serializable
public data class DonationUrl(
    val id: String? = null,
    val platform: String? = null,
    val url: String
)

@Serializable
public data class GalleryItem(
    val url: String,
    val featured: Boolean,
    val title: String? = null,
    val description: String? = null,
    val created: String
)

@Serializable
public data class ProjectLicense(
    val id: String,
    val name: String? = null,
    val url: String? = null
)
