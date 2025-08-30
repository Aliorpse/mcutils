package tech.aliorpse.mcutils.utils

import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tech.aliorpse.mcutils.model.player.PlayerProfileAdapter
import tech.aliorpse.mcutils.modules.modrinth.ModrinthService
import tech.aliorpse.mcutils.modules.player.PlayerService

internal object HttpClient {
    private val moshi = Moshi.Builder()
        .add(PlayerProfileAdapter())
        .build()

    private val moshiFactory = MoshiConverterFactory.create(moshi)

    val mojangProfileService: PlayerService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.mojang.com/")
            .addConverterFactory(moshiFactory)
            .build()
            .create(PlayerService::class.java)
    }

    val mojangSessionService: PlayerService by lazy {
        Retrofit.Builder()
            .baseUrl("https://sessionserver.mojang.com/")
            .addConverterFactory(moshiFactory)
            .build()
            .create(PlayerService::class.java)
    }

    val modrinthService: ModrinthService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.modrinth.com/")
            .addConverterFactory(moshiFactory)
            .build()
            .create(ModrinthService::class.java)
    }
}
