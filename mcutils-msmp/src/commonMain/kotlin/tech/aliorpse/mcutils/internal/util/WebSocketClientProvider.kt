package tech.aliorpse.mcutils.internal.util

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal object WebSocketClientProvider {
    val webSocketClient: HttpClient by lazy {
        HttpClient {
            install(HttpTimeout)
            install(ContentNegotiation) { json(Json) }
            install(WebSockets) {
                pingIntervalMillis = 10000L
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        }
    }
}
