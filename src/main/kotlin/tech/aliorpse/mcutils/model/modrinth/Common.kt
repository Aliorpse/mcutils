package tech.aliorpse.mcutils.model.modrinth

import com.squareup.moshi.Json

/**
 * Project types.
 */
enum class ProjectType(val value: String) {
    @Json(name = "mod") MOD("mod"),
    @Json(name = "modpack") MODPACK("modpack"),
    @Json(name = "resourcepack") RESOURCEPACK("resourcepack"),
    @Json(name = "shader") SHADER("shader")
}

/**
 * Client/server side support.
 */
enum class SideSupport(val value: String) {
    @Json(name = "required") REQUIRED("required"),
    @Json(name = "optional") OPTIONAL("optional"),
    @Json(name = "unsupported") UNSUPPORTED("unsupported")
}

/**
 * Index/sorting methods in search.
 */
enum class IndexMethod(val value: String) {
    @Json(name = "relevance") RELEVANCE("relevance"),
    @Json(name = "downloads") DOWNLOADS("downloads"),
    @Json(name = "follows") FOLLOWS("follows"),
    @Json(name = "newest") NEWEST("newest"),
    @Json(name = "updated") UPDATED("updated")
}

/**
 * Status of a project.
 */
enum class ProjectStatus(val value: String) {
    @Json(name = "approved") APPROVED("approved"),
    @Json(name = "archived") ARCHIVED("archived"),
    @Json(name = "rejected") REJECTED("rejected"),
    @Json(name = "draft") DRAFT("draft"),
    @Json(name = "unlisted") UNLISTED("unlisted"),
    @Json(name = "processing") PROCESSING("processing"),
    @Json(name = "withheld") WITHHELD("withheld"),
    @Json(name = "scheduled") SCHEDULED("scheduled"),
    @Json(name = "private") PRIVATE("private"),
    @Json(name = "unknown") UNKNOWN("unknown")
}

/**
 * Requested status for review or release scheduling.
 */
enum class RequestedStatus(val value: String) {
    @Json(name = "approved") APPROVED("approved"),
    @Json(name = "archived") ARCHIVED("archived"),
    @Json(name = "unlisted") UNLISTED("unlisted"),
    @Json(name = "private") PRIVATE("private"),
    @Json(name = "draft") DRAFT("draft")
}

/**
 * Monetization status.
 */
enum class MonetizationStatus(val value: String) {
    @Json(name = "monetized") MONETIZED("monetized"),
    @Json(name = "demonetized") DEMONETIZED("demonetized"),
    @Json(name = "force-demonetized") FORCE_DEMONETIZED("force-demonetized")
}
