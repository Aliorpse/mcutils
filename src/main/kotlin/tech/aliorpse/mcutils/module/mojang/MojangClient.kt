package tech.aliorpse.mcutils.module.mojang

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tech.aliorpse.mcutils.model.mojang.PlayerProfileAdapter

/**
 * Internal object responsible for managing the communication with Mojang's APIs.
 *
 * This object acts as a client for Mojang's API services, providing access to Mojang's profile
 * and session endpoints. It sets up and configures the necessary Retrofit instances and Moshi
 * deserialization logic to facilitate communication with these endpoints.
 *
 * The MojangClient offers two primary services:
 * - `profileService`: Used to interact with the base Mojang API endpoints at "https://api.mojang.com/".
 * - `sessionService`: Used to interact with Mojang's session server at "https://sessionserver.mojang.com/".
 */
internal object MojangClient {
    private val moshi = Moshi.Builder()
        .add(PlayerProfileAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    private val moshiFactory = MoshiConverterFactory.create(moshi)

    val profileService: MojangService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.mojang.com/")
            .addConverterFactory(moshiFactory)
            .build()
            .create(MojangService::class.java)
    }

    val sessionService: MojangService by lazy {
        Retrofit.Builder()
            .baseUrl("https://sessionserver.mojang.com/")
            .addConverterFactory(moshiFactory)
            .build()
            .create(MojangService::class.java)
    }
}
