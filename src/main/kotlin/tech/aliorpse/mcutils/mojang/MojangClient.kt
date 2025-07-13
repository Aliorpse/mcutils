package tech.aliorpse.mcutils.mojang

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tech.aliorpse.mcutils.model.mojang.PlayerProfile
import tech.aliorpse.mcutils.model.mojang.PlayerProfileDeserializer

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
