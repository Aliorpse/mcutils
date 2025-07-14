package tech.aliorpse.mcutils.module.mojang

import tech.aliorpse.mcutils.model.mojang.PlayerProfile

/**
 * A utility object for interacting with the Mojang API to retrieve player profile data.
 */
object MojangAPI {
    /**
     * Retrieves the player's profile from the Mojang session server based on the provided UUID.
     *
     * @param uuid The player's uuid.
     * @return `PlayerProfile` containing detailed player information including id, name, skin, cape, and model type.
     */
    suspend fun getProfile(uuid: String): PlayerProfile =
        MojangClient.sessionService.getProfile(uuid)

    /**
     * Another version of [getProfile].
     *
     * @param username The player's username.
     * @return `PlayerProfile` containing detailed player information including id, name, skin, cape, and model type.
     */
    suspend fun getProfileByName(username: String): PlayerProfile {
        val uuid = MojangClient.profileService.getUUID(username).id
        return MojangClient.sessionService.getProfile(uuid)
    }
}
