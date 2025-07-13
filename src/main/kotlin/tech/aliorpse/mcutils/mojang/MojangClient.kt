package tech.aliorpse.mcutils.mojang

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tech.aliorpse.mcutils.model.mojang.PlayerProfile
import tech.aliorpse.mcutils.model.mojang.PlayerProfileDeserializer

/**
 * Internal object responsible for managing the communication with Mojang's APIs.
 *
 * This object acts as a client for Mojang's API services, providing access to Mojang's profile
 * and session endpoints. It sets up and configures the necessary Retrofit instances and Gson
 * deserialization logic to facilitate communication with these endpoints.
 *
 * The MojangClient offers two primary services:
 * - `profileService`: Used to interact with the base Mojang API endpoints at "https://api.mojang.com/".
 * - `sessionService`: Used to interact with Mojang's session server at "https://sessionserver.mojang.com/".
 */
internal object MojangClient {
    private val gson = GsonBuilder()
        .registerTypeAdapter(PlayerProfile::class.java, PlayerProfileDeserializer())
        .create()
    private val gsonFactory = GsonConverterFactory.create(gson)

    val profileService: MojangService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.mojang.com/")
            .addConverterFactory(gsonFactory)
            .build()
            .create(MojangService::class.java)
    }

    val sessionService: MojangService by lazy {
        Retrofit.Builder()
            .baseUrl("https://sessionserver.mojang.com/")
            .addConverterFactory(gsonFactory)
            .build()
            .create(MojangService::class.java)
    }
}
