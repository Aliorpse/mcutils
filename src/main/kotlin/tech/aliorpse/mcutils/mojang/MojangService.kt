package tech.aliorpse.mcutils.mojang

import retrofit2.http.GET
import retrofit2.http.Path
import tech.aliorpse.mcutils.model.mojang.PlayerProfile
import tech.aliorpse.mcutils.model.mojang.PlayerUUIDProfile

interface MojangService {
    @GET("/session/minecraft/profile/{uuid}")
    suspend fun getProfile(
        @Path("uuid") uuid: String
    ): PlayerProfile

    @GET("/users/profiles/minecraft/{username}")
    suspend fun getUUID(
        @Path("username") username: String
    ): PlayerUUIDProfile
}