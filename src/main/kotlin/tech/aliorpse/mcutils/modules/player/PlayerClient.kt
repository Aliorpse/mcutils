package tech.aliorpse.mcutils.modules.player

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tech.aliorpse.mcutils.model.player.PlayerProfileAdapter

internal object PlayerClient {
    private val moshi = Moshi.Builder()
        .add(PlayerProfileAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    private val moshiFactory = MoshiConverterFactory.create(moshi)

    val profileService: PlayerService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.mojang.com/")
            .addConverterFactory(moshiFactory)
            .build()
            .create(PlayerService::class.java)
    }

    val sessionService: PlayerService by lazy {
        Retrofit.Builder()
            .baseUrl("https://sessionserver.mojang.com/")
            .addConverterFactory(moshiFactory)
            .build()
            .create(PlayerService::class.java)
    }
}
