@file:Suppress("unused")
package tech.aliorpse.mcutils.model.modrinth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Project types.
 */
@Serializable
public enum class ProjectType(public val value: String) {
    @SerialName("mod") MOD("mod"),
    @SerialName("modpack") MODPACK("modpack"),
    @SerialName("resourcepack") RESOURCEPACK("resourcepack"),
    @SerialName("shader") SHADER("shader")
}

/**
 * Client/server side support.
 */
@Serializable
public enum class SideSupport(public val value: String) {
    @SerialName("required") REQUIRED("required"),
    @SerialName("optional") OPTIONAL("optional"),
    @SerialName("unsupported") UNSUPPORTED("unsupported"),
    @SerialName("unknown") UNKNOWN("unknown")
}

/**
 * Index/sorting methods in search.
 */
@Serializable
public enum class IndexMethod(public val value: String) {
    @SerialName("relevance") RELEVANCE("relevance"),
    @SerialName("downloads") DOWNLOADS("downloads"),
    @SerialName("follows") FOLLOWS("follows"),
    @SerialName("newest") NEWEST("newest"),
    @SerialName("updated") UPDATED("updated")
}

/**
 * Status of a project.
 */
@Serializable
public enum class ProjectStatus(public val value: String) {
    @SerialName("approved") APPROVED("approved"),
    @SerialName("archived") ARCHIVED("archived"),
    @SerialName("rejected") REJECTED("rejected"),
    @SerialName("draft") DRAFT("draft"),
    @SerialName("unlisted") UNLISTED("unlisted"),
    @SerialName("processing") PROCESSING("processing"),
    @SerialName("withheld") WITHHELD("withheld"),
    @SerialName("scheduled") SCHEDULED("scheduled"),
    @SerialName("private") PRIVATE("private"),
    @SerialName("unknown") UNKNOWN("unknown")
}

/**
 * Requested status for review or release scheduling.
 */
@Serializable
public enum class RequestedStatus(public val value: String) {
    @SerialName("approved") APPROVED("approved"),
    @SerialName("archived") ARCHIVED("archived"),
    @SerialName("unlisted") UNLISTED("unlisted"),
    @SerialName("private") PRIVATE("private"),
    @SerialName("draft") DRAFT("draft")
}

/**
 * Monetization status.
 */
@Serializable
public enum class MonetizationStatus(public val value: String) {
    @SerialName("monetized") MONETIZED("monetized"),
    @SerialName("demonetized") DEMONETIZED("demonetized"),
    @SerialName("force-demonetized") FORCE_DEMONETIZED("force-demonetized")
}
