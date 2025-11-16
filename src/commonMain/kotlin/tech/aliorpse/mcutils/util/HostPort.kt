package tech.aliorpse.mcutils.util

public data class HostPort(
    val host: String?,
    val port: Int?
)

/**
 * Parses a host:port? string into [HostPort].
 *
 * - Empty input → HostPort(null, null)
 * - Invalid ports → null
 */
public fun hostPortOf(address: String?): HostPort {
    if (address.isNullOrBlank()) return HostPort(null, null)

    return if (address.startsWith("[")) {
        // IPv6
        val closing = address.indexOf(']')
        require(closing != -1) { "Invalid IPv6 address: $address" }
        val host = address.substring(1, closing)
        val port = address.substring(closing + 1)
            .takeIf { it.startsWith(":") }
            ?.drop(1)
            ?.toIntOrNull()
        HostPort(host, port)
    } else {
        // IPv4 / domain
        val parts = address.split(":", limit = 2)
        val host = parts[0]
        val port = parts.getOrNull(1)?.toIntOrNull()
        HostPort(host, port)
    }
}
