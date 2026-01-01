package tech.aliorpse.mcutils.api.registry

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import tech.aliorpse.mcutils.api.MsmpConnection
import tech.aliorpse.mcutils.api.extension.ArrayExtension
import tech.aliorpse.mcutils.api.extension.GamerulesExtension
import tech.aliorpse.mcutils.api.extension.PlayersExtension
import tech.aliorpse.mcutils.api.extension.ServerExtension
import tech.aliorpse.mcutils.api.extension.ServerSettingsExtension
import tech.aliorpse.mcutils.entity.OperatorDto
import tech.aliorpse.mcutils.entity.PlayerDto
import tech.aliorpse.mcutils.entity.UserBanDto
import kotlin.properties.ReadOnlyProperty

/**
 * Registry for MSMP request extension.
 *
 * This variant automatically injects [registryName] and the [KSerializer] for [T] into the [factory].
 *
 * Example:
 * ```kotlin
 * public val MsmpConnection.allowList: ArrayExtension<PlayerDto>
 *     by msmpExtension("minecraft:allowlist", ::ArrayExtension)
 * ```
 */
@Suppress("UNCHECKED_CAST")
public inline fun <reified T : Any, R : MsmpExtension> msmpExtension(
    registryName: String,
    crossinline factory: (MsmpConnection, String, KSerializer<T>) -> R
): ReadOnlyProperty<MsmpConnection, R> = ReadOnlyProperty { thisRef, _ ->
    thisRef.callExtensions.getOrPut(registryName) {
        factory(thisRef, registryName, serializer<T>())
    } as R
}

/**
 * Registry for MSMP request extension.
 *
 * This variant automatically injects [registryName] into the [factory].
 *
 * Use this for extensions that don't use generic types.
 *
 * Example:
 * ```kotlin
 * public val MsmpConnection.server: ServerExtension
 *     by msmpExtension("minecraft:server", ::ServerExtension)
 * ```
 */
@Suppress("UNCHECKED_CAST")
public fun <R : MsmpExtension> msmpExtension(
    registryName: String,
    factory: (MsmpConnection, String) -> R
): ReadOnlyProperty<MsmpConnection, R> = ReadOnlyProperty { thisRef, _ ->
    thisRef.callExtensions.getOrPut(registryName) {
        factory(thisRef, registryName)
    } as R
}

public interface MsmpExtension {
    public val connection: MsmpConnection
    public val baseEndpoint: String
}

public val MsmpConnection.allowList: ArrayExtension<PlayerDto>
    by msmpExtension("minecraft:allowlist", ::ArrayExtension)

public val MsmpConnection.banList: ArrayExtension<UserBanDto>
    by msmpExtension("minecraft:bans", ::ArrayExtension)

public val MsmpConnection.operatorList: ArrayExtension<OperatorDto>
    by msmpExtension("minecraft:operators", ::ArrayExtension)

public val MsmpConnection.players: PlayersExtension
    by msmpExtension("minecraft:players", ::PlayersExtension)

public val MsmpConnection.server: ServerExtension
    by msmpExtension("minecraft:server", ::ServerExtension)

public val MsmpConnection.gamerules: GamerulesExtension
    by msmpExtension("minecraft:gamerules", ::GamerulesExtension)

public val MsmpConnection.serverSettings: ServerSettingsExtension
    by msmpExtension("minecraft:serversettings", ::ServerSettingsExtension)
