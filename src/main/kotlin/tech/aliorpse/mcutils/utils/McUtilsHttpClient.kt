package tech.aliorpse.mcutils.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

public object McUtilsHttpClient {
    internal lateinit var client: HttpClient
        private set

    public fun init(engine: HttpClientEngineFactory<*>) {
        if (::client.isInitialized) return

        client = HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
}
