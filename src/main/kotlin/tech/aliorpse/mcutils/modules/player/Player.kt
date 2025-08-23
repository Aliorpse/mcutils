package tech.aliorpse.mcutils.modules.player

import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.model.player.PlayerProfile

object Player {
    const val UUID_LENGTH = 32
    val nameRegex = Regex("^[A-Za-z0-9_]{3,16}$")

    /**
     * Fetches a player's profile from Mojang's session server.
     *
     * The input can be either:
     * - A UUID (with or without dashes, 32–36 characters), or
     * - A valid Minecraft username (3–16 characters, letters, digits, and underscores).
     *
     * @param player The player's UUID or username.
     * @return A [PlayerProfile] containing the player's UUID, username, skin, cape, and model type.
     * @throws IllegalArgumentException if the input is neither a valid UUID nor a valid username.
     */
    suspend fun getProfile(player: String): PlayerProfile {
        val pl = player.replace("-", "")

        return when {
            pl.length == UUID_LENGTH -> {
                PlayerClient.profileService.getProfile(pl)
            }

            nameRegex.matches(pl) -> {
                val uuid = PlayerClient.sessionService.getUUID(pl).id
                PlayerClient.sessionService.getProfile(uuid)
            }

            else -> throw IllegalArgumentException("Invalid identifier: $pl")
        }
    }

    /**
     * Blocking method of [getProfile].
     */
    @JvmStatic
    fun getProfileBlocking(player: String): PlayerProfile = runBlocking {
        getProfile(player)
    }
}
