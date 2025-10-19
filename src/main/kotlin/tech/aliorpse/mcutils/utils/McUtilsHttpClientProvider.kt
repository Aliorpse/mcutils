package tech.aliorpse.mcutils.utils

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal object McUtilsHttpClientProvider {
    private val delegate = HttpClientDelegate()

    val httpClient: HttpClient
        get() = delegate.getClient()

    private fun makeClient(engine: HttpClientEngineFactory<*>): HttpClient =
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }

            install(WebSockets)
        }

    private class HttpClientDelegate {
        private val availableClients = listOf(
            "io.ktor.client.engine.cio.CIO",
            "io.ktor.client.engine.okhttp.OkHttp",
            "io.ktor.client.engine.jetty.Jetty",
            "io.ktor.client.engine.java.Java",
            "io.ktor.client.engine.apache5.Apache5",
        )

        @Volatile private var client: HttpClient? = null

        fun getClient(): HttpClient {
            client?.let { return it }

            val initialized = synchronized(this) {
                client?.let { return@synchronized it }

                for (clientName in availableClients) {
                    val clazz = runCatching { Class.forName(clientName) }.getOrNull() ?: continue

                    val engine = run {
                        val instanceField = clazz.getDeclaredField("INSTANCE")
                        instanceField.isAccessible = true
                        instanceField.get(null) as HttpClientEngineFactory<*>
                    }

                    val newClient = makeClient(engine)
                    client = newClient
                    return@synchronized newClient
                }

                null
            }

            return initialized ?: error("No available ktor engine found in classpath")
        }

        fun setClient(value: HttpClient) {
            if (client != null) error("HttpClient has already been initialized")
            client = value
        }
    }

    internal fun use(engine: HttpClientEngineFactory<*>): Unit = delegate.setClient(makeClient(engine))
}
