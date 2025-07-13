package tech.aliorpse.mcutils.mojang

import retrofit2.http.GET
import retrofit2.http.Path
import tech.aliorpse.mcutils.model.mojang.PlayerProfile
import tech.aliorpse.mcutils.model.mojang.PlayerUUIDProfile

/**
 * Defines the MojangService interface for interacting with Mojang's web APIs.
 */
interface MojangService {
    /**
     * Retrieves the player's profile from the Mojang session server using their UUID.
     *
     * @param uuid The UUID of the player whose profile is to be retrieved.
     * @return The `PlayerProfile` containing detailed player information such as id,
     * name, skin URL, cape URL, and skin model type.
     */
    @GET("/session/minecraft/profile/{uuid}")
    suspend fun getProfile(
        @Path("uuid") uuid: String
    ): PlayerProfile

    /**
     * Retrieves the player's UUID profile from the Mojang API based on the provided username.
     *
     * @param username The username of the player whose UUID profile is to be retrieved.
     * @return The `PlayerUUIDProfile` containing the player's UUID and name, along with flags
     * indicating legacy or demo accounts.
     */
    @GET("/users/profiles/minecraft/{username}")
    suspend fun getUUID(
        @Path("username") username: String
    ): PlayerUUIDProfile
}