package tech.aliorpse.mcutils.internal.entity

import kotlinx.serialization.Serializable

@Serializable
internal data class PlayerProfileResponse(
    val id: String,
    val name: String,
    val legacy: Boolean = false,
    val properties: List<PlayerProperty>
)

@Serializable
internal data class PlayerProperty(
    val name: String,
    val value: String,
    val signature: String? = null
)

@Serializable
internal data class DecodedTextures(
    val timestamp: Long? = null,
    val profileId: String? = null,
    val profileName: String? = null,
    val textures: Map<String, Texture>
)

@Serializable
internal data class Texture(
    val url: String,
    val metadata: Metadata? = null
)

@Serializable
internal data class Metadata(
    val model: String? = null
)
