package tech.aliorpse.mcutils.internal.util

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal object WebSocketClientProvider {
    val webSocketClient: HttpClient by lazy {
        HttpClient {
            install(HttpTimeout)
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(WebSockets) {
                pingIntervalMillis = 10000L
            }
        }
    }
}
