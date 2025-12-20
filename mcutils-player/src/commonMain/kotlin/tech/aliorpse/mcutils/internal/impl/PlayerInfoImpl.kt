package tech.aliorpse.mcutils.internal.impl

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import tech.aliorpse.mcutils.entity.PlayerProfile
import tech.aliorpse.mcutils.entity.SkinModel
import tech.aliorpse.mcutils.internal.entity.DecodedTextures
import tech.aliorpse.mcutils.internal.entity.PlayerProfileResponse
import tech.aliorpse.mcutils.internal.util.HttpClientProvider.httpClient
import tech.aliorpse.mcutils.internal.util.withDispatchersIO

internal object PlayerInfoImpl {
    const val UUID_LENGTH = 32
    const val MOJANG_PROFILE_BASE = "https://api.mojang.com"
    const val MOJANG_SESSION_BASE = "https://sessionserver.mojang.com"

    private val json = Json { ignoreUnknownKeys = true }
    private val nameRegex = Regex("^[A-Za-z0-9_]{3,16}$")

    suspend fun getProfile(player: String) = withDispatchersIO {
        val player = player.replace("-", "")

        val id = when {
            player.length == UUID_LENGTH -> player
            nameRegex.matches(player) -> getUuid(player)
            else -> throw IllegalArgumentException("Invalid identifier: $player")
        }

        val rawProfile: PlayerProfileResponse =
            httpClient.get("$MOJANG_SESSION_BASE/session/minecraft/profile/$id").body()

        val decoded = json.decodeFromString<DecodedTextures>(
            rawProfile.properties.first().value.decodeBase64Bytes().decodeToString()
        )

        PlayerProfile(
            rawProfile.id,
            rawProfile.name,
            rawProfile.legacy,
            decoded.textures["SKIN"]?.url,
            decoded.textures["CAPE"]?.url,
            SkinModel.from(decoded.textures["SKIN"]?.metadata?.model)
        )
    }

    suspend fun getUuid(playerName: String) = withDispatchersIO {
        httpClient.get("$MOJANG_PROFILE_BASE/users/profiles/minecraft/$playerName")
            .bodyAsText().let { json.parseToJsonElement(it.trim()) }
            .jsonObject["id"]!!.jsonPrimitive.content
    }
}
