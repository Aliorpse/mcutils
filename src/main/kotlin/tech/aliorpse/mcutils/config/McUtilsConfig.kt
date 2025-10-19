package tech.aliorpse.mcutils.config

import io.ktor.client.engine.*
import tech.aliorpse.mcutils.modules.server.status.JavaServer
import tech.aliorpse.mcutils.utils.McUtilsHttpClientProvider
import tech.aliorpse.mcutils.utils.defaultResolveSrvRecord

public object McUtilsConfig {
    @JvmStatic
    public val dns: McUtilsDnsConfig = McUtilsDnsConfig()
    @JvmStatic
    public val httpClient: McUtilsHttpClientConfig = McUtilsHttpClientConfig()
}

public class McUtilsDnsConfig internal constructor() {
    /**
     * Custom SRV resolver used in [JavaServer]
     */
    public var srvResolver: suspend (String) -> Pair<String, Int>? = ::defaultResolveSrvRecord
}


public class McUtilsHttpClientConfig internal constructor() {
    /**
     * Overrides the default http client with a custom engine.
     *
     * Must be called **before** the first access of http client (calling of http-related functions).
     * It should be only called **once**.
     *
     * @throws IllegalStateException If called after the first access of http client.
     */
    public fun use(engine: HttpClientEngineFactory<*>): Unit = McUtilsHttpClientProvider.use(engine)
}
