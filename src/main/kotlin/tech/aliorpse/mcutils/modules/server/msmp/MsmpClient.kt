package tech.aliorpse.mcutils.modules.server.msmp

import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import tech.aliorpse.mcutils.utils.McUtilsHttpClientProvider.httpClient
import tech.aliorpse.mcutils.utils.withDispatchersIO

/**
 * @param url e.g. "ws://localhost:25585"
 * @param token token without "Bearer "
 */
public class MsmpClient(
    private val url: String,
    private val token: String
) {
    /**
     * Connect to the server, return the connection.
     */
    @JvmAsync
    @JvmBlocking
    public suspend fun connect(): MsmpConnection = withDispatchersIO {
        val client = httpClient
        val session = client.webSocketSession {
            url(this@MsmpClient.url)
            headers.append("Authorization", "Bearer ${this@MsmpClient.token}")
        }
        MsmpConnection(session, token)
    }

    public companion object {
        /**
         * Connect directly using a URL and token without creating an explicit [MsmpClient].
         *
         * @param url e.g. "ws://localhost:25585"
         * @param token token without "Bearer "
         */
        @JvmStatic
        @JvmAsync
        @JvmBlocking
        public suspend fun connect(url: String, token: String): MsmpConnection =
            MsmpClient(url, token).connect()
    }

    /**
     * Creates a temporary [MsmpConnection], runs [block], then closes it automatically.
     *
     * Only accessible in Kotlin.
     */
    @JvmSynthetic
    public suspend fun withConnection(block: suspend MsmpConnection.() -> Unit) {
        val connection = connect()
        block(connection)
        connection.close()
    }
}
