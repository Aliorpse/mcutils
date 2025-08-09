package tech.aliorpse.mcutils.modules.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.model.player.PlayerProfile

object Player {
    /**
     * Retrieves the player's profile from the Mojang session server based on the provided UUID.
     *
     * @param uuid The player's uuid.
     * @return `PlayerProfile` containing detailed player information including id, name, skin, cape, and model type.
     */
    suspend fun getProfile(uuid: String): PlayerProfile =
        PlayerClient.sessionService.getProfile(uuid)

    /**
     * Blocking method of [getProfile].
     */
    @JvmStatic
    fun getProfileBlocking(uuid: String): PlayerProfile = runBlocking(Dispatchers.IO) {
        PlayerClient.sessionService.getProfile(uuid)
    }

    /**
     * Another version of [getProfile].
     *
     * @param username The player's username.
     * @return `PlayerProfile` containing detailed player information including id, name, skin, cape, and model type.
     */
    suspend fun getProfileByName(username: String): PlayerProfile {
        val uuid = PlayerClient.profileService.getUUID(username).id
        return PlayerClient.sessionService.getProfile(uuid)
    }

    /**
     * Blocking method of [getProfileByName]
     */
    @JvmStatic
    fun getProfileByNameBlocking(username: String): PlayerProfile = runBlocking(Dispatchers.IO) {
        getProfileByName(username)
    }
}
