package tech.aliorpse.mcutils.entity

import kotlinx.serialization.Serializable

/**
 * Server List Ping status.
 *
 * @property secureChatEnforced Whether secure chat is required.
 * @property favicon Base64-encoded favicon, if present.
 * @property ping Ping in milliseconds, null if unsupported.
 * @property srvRecord  e.g. int-p-as-1.wdsj.net:55581
 */
@Serializable
public data class ServerStatus(
    val description: TextComponent,
    val players: Players,
    val version: Version,
    val ping: Long,
    val secureChatEnforced: Boolean,
    val favicon: String?,
    val srvRecord: String?
)

public interface QueryStatus {
    public val description: String
    public val map: String
    public val players: Players
}

/**
 * Query basic status.
 *
 * @property map Name of the current map.
 */
@Serializable
public data class QueryStatusBasic(
    override val description: String,
    override val map: String,
    override val players: Players,
) : QueryStatus

/**
 * Query full status.
 *
 * @property map Name of the current map.
 */
@Serializable
public data class QueryStatusFull(
    override val description: String,
    override val map: String,
    override val players: Players,
    val version: String,
    val plugins: Set<String>,
) : QueryStatus

/**
 * Player info.
 *
 * @property sample Optional sample list of online players.
 */
@Serializable
public data class Players(
    val max: Int,
    val online: Int,
    val sample: Set<Sample>? = emptySet()
)

/**
 * Version info.
 *
 * @property name Version name.
 * @property protocol Protocol number.
 */
@Serializable
public data class Version(
    val name: String,
    val protocol: Long
)

/**
 * Sample player entry.
 */
@Serializable
public data class Sample(
    val id: String,
    val name: String
)
