package tech.aliorpse.mcutils.api.registry

import tech.aliorpse.mcutils.api.MsmpConnection
import tech.aliorpse.mcutils.api.extension.GamerulesExtension
import tech.aliorpse.mcutils.api.extension.PlayersExtension
import tech.aliorpse.mcutils.api.extension.ServerExtension
import tech.aliorpse.mcutils.api.extension.ServerSettingsExtension
import tech.aliorpse.mcutils.api.extension.UniversalArrayExtension
import tech.aliorpse.mcutils.entity.OperatorDto
import tech.aliorpse.mcutils.entity.PlayerDto
import tech.aliorpse.mcutils.entity.UserBanDto
import kotlin.properties.ReadOnlyProperty

/**
 * Registry for MSMP request extensions.
 *
 * ```kotlin
 * public val MsmpConnection.allowList: UniversalArrayExtension<PlayerDto>
 *     by msmpExtension("minecraft:allowlist") { UniversalArrayExtension(it, this) }
 * ```
 *
 * In this case, `it` refers to the [MsmpConnection] instance, `this` refers to the registry name.
 *
 * Of course, you can write like this too:
 *
 * ```kotlin
 * public val MsmpConnection.server: ServerExtension
 *     by msmpExtension("minecraft:server") { ServerExtension(it) }
 * ```
 */
public inline fun <reified T : Any> msmpExtension(
    registryName: String,
    crossinline factory: String.(MsmpConnection) -> T
): ReadOnlyProperty<MsmpConnection, T> {
    return ReadOnlyProperty { thisRef, _ ->
        thisRef.callExtensions.getOrPut(registryName) {
            registryName.factory(thisRef)
        } as T
    }
}

public val MsmpConnection.allowList: UniversalArrayExtension<PlayerDto>
    by msmpExtension("minecraft:allowlist") { UniversalArrayExtension(it, this) }

public val MsmpConnection.banList: UniversalArrayExtension<UserBanDto>
    by msmpExtension("minecraft:bans") { UniversalArrayExtension(it, this) }

public val MsmpConnection.operatorList: UniversalArrayExtension<OperatorDto>
    by msmpExtension("minecraft:operators") { UniversalArrayExtension(it, this) }

public val MsmpConnection.players: PlayersExtension
    by msmpExtension("minecraft:players") { PlayersExtension(it) }

public val MsmpConnection.server: ServerExtension
    by msmpExtension("minecraft:server") { ServerExtension(it) }

public val MsmpConnection.gamerules: GamerulesExtension
    by msmpExtension("minecraft:gamerules") { GamerulesExtension(it) }

public val MsmpConnection.serverSettings: ServerSettingsExtension
    by msmpExtension("minecraft:serversettings") { ServerSettingsExtension(it) }
