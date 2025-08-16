package tech.aliorpse.mcutils.modules.player

import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.model.player.PlayerProfile

object Player {
    /**
     * UUID or NAME.
     */
    enum class IDType {
        UUID, NAME
    }

    /**
     * Retrieves the player's profile from the Mojang session server.
     *
     * @param id Either UUID or name.
     * @return `PlayerProfile` containing detailed player information including id, name, skin, cape, and model type.
     */
    suspend fun getProfile(id: String, type: IDType): PlayerProfile {
        return when (type) {
            IDType.UUID -> PlayerClient.sessionService.getProfile(id)

            IDType.NAME -> {
                val id = PlayerClient.profileService.getUUID(id).id
                PlayerClient.sessionService.getProfile(id)
            }
        }
    }

    /**
     * Blocking method of [getProfile].
     */
    @JvmStatic
    fun getProfileBlocking(id: String, type: IDType): PlayerProfile = runBlocking {
        getProfile(id, type)
    }
}
