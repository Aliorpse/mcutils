package tech.aliorpse.mcutils.modules.player

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.json.Json
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import tech.aliorpse.mcutils.model.player.PlayerProfile
import tech.aliorpse.mcutils.model.player.SkinModel
import tech.aliorpse.mcutils.model.player.internal.DecodedTextures
import tech.aliorpse.mcutils.model.player.internal.PlayerUUIDProfile
import tech.aliorpse.mcutils.model.player.internal.RawPlayerProfile
import tech.aliorpse.mcutils.utils.McUtilsHttpClientProvider.httpClient
import tech.aliorpse.mcutils.utils.withDispatchersIO

/**
 * Utility object for fetching Minecraft player profiles.
 *
 * Supports fetching by UUID or username, and automatically resolves
 * UUIDs when given a username.
 */
public object Player {
    private val json = Json { ignoreUnknownKeys = true }
    private const val UUID_LENGTH = 32
    private val nameRegex = Regex("^[A-Za-z0-9_]{3,16}$")

    private const val MOJANG_PROFILE_BASE = "https://api.mojang.com"
    private const val MOJANG_SESSION_BASE = "https://sessionserver.mojang.com"

    /**
     * Fetches a player's profile from Mojang's session server.
     *
     * The input can be either:
     * - A UUID (with or without dashes, 32–36 characters), or
     * - A valid Minecraft username (3–16 characters, letters, digits, and underscores).
     *
     * @param player The player's UUID or username.
     * @throws IllegalArgumentException if the input is neither a valid UUID nor a valid username.
     */
    @JvmStatic
    @JvmAsync
    @JvmBlocking
    public suspend fun getProfile(player: String): PlayerProfile = withDispatchersIO {
        val pl = player.replace("-", "")

        val raw: RawPlayerProfile = when {
            pl.length == UUID_LENGTH -> {
                httpClient.get("$MOJANG_SESSION_BASE/session/minecraft/profile/$pl").body()
            }

            nameRegex.matches(pl) -> {
                val uuidProfile: PlayerUUIDProfile =
                    httpClient.get("$MOJANG_PROFILE_BASE/users/profiles/minecraft/$pl").body()
                httpClient.get("$MOJANG_SESSION_BASE/session/minecraft/profile/${uuidProfile.id}").body()
            }

            else -> throw IllegalArgumentException("Invalid identifier: $pl")
        }

        val decoded = json.decodeFromString<DecodedTextures>(
            String(raw.properties.first().value.decodeBase64Bytes())
        )

        PlayerProfile(
            raw.id,
            raw.name,
            raw.legacy,
            decoded.textures["SKIN"]?.url,
            decoded.textures["CAPE"]?.url,
            SkinModel.from(decoded.textures["SKIN"]?.metadata?.model)
        )
    }
}
