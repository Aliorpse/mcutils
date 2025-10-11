package tech.aliorpse.mcutils.utils

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import tech.aliorpse.mcutils.utils.McUtilsHttpClientProvider.httpClient
import tech.aliorpse.mcutils.utils.McUtilsHttpClientProvider.use
import kotlin.reflect.KProperty

/**
 * Provides the [HttpClient] used internally.
 *
 * By default, the client will be initialized lazily by detecting
 * the first available engine on the classpath in the following order:
 *
 * - CIO
 * - OkHttp
 * - Jetty
 * - Java
 * - Apache5
 *
 * In most cases no configuration is required.
 * To override this, call [use] before the first access.
 *
 * Example:
 * ```kotlin
 * McUtilsHttpClientProvider.use(OkHttp)
 * ```
 */
public object McUtilsHttpClientProvider {
    private val delegate = HttpClientDelegate()

    internal val httpClient: HttpClient by delegate

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

        operator fun getValue(thisRef: Any?, property: KProperty<*>): HttpClient {
            client?.let { return it }

            val initialized = synchronized(this) {
                client?.let { return@synchronized it }

                for (clientName in availableClients) {
                    val clazz = runCatching { Class.forName(clientName) }.getOrNull() ?: continue
                    val engine = clazz.kotlin.objectInstance as HttpClientEngineFactory<*>

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

    /**
     * Overrides the default [HttpClient] with a custom engine.
     *
     * Must be called **before** the first access of [httpClient] (calling of http-related functions).
     * It should be only called **once**.
     *
     * @throws IllegalStateException If called after the first access of [httpClient].
     */
    public fun use(engine: HttpClientEngineFactory<*>): Unit = delegate.setClient(makeClient(engine))
}
