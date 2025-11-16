package tech.aliorpse.mcutils.internal.util

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.rpc.krpc.ktor.client.Krpc
import kotlinx.serialization.json.Json

internal object HttpClientProvider {
    val httpClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }

            install(Krpc)
        }
    }
}
