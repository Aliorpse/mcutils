package tech.aliorpse.mcutils.api.registry

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import tech.aliorpse.mcutils.api.MsmpClient
import tech.aliorpse.mcutils.api.MsmpState
import tech.aliorpse.mcutils.api.extension.ArrayExtension
import tech.aliorpse.mcutils.api.extension.GamerulesExtension
import tech.aliorpse.mcutils.api.extension.PlayersExtension
import tech.aliorpse.mcutils.api.extension.ServerExtension
import tech.aliorpse.mcutils.api.extension.ServerSettingsExtension
import tech.aliorpse.mcutils.entity.AllowlistAddedEvent
import tech.aliorpse.mcutils.entity.AllowlistRemovedEvent
import tech.aliorpse.mcutils.entity.GameruleUpdatedEvent
import tech.aliorpse.mcutils.entity.IPBanAddedEvent
import tech.aliorpse.mcutils.entity.IPBanDto
import tech.aliorpse.mcutils.entity.IPBanRemovedEvent
import tech.aliorpse.mcutils.entity.MsmpEvent
import tech.aliorpse.mcutils.entity.OperatorAddedEvent
import tech.aliorpse.mcutils.entity.OperatorDto
import tech.aliorpse.mcutils.entity.OperatorRemovedEvent
import tech.aliorpse.mcutils.entity.PlayerDto
import tech.aliorpse.mcutils.entity.PlayerJoinedEvent
import tech.aliorpse.mcutils.entity.PlayerLeftEvent
import tech.aliorpse.mcutils.entity.UserBanAddedEvent
import tech.aliorpse.mcutils.entity.UserBanDto
import tech.aliorpse.mcutils.entity.UserBanRemovedEvent
import kotlin.properties.ReadOnlyProperty

/**
 * Registry for MSMP request extension.
 *
 * This variant automatically injects [registryName] and the [KSerializer] for [T] into the [factory].
 *
 * Example:
 * ```kotlin
 * public val MsmpClient.allowList: ArrayExtension<PlayerDto>
 *     by msmpExtension("minecraft:allowlist", ::ArrayExtension)
 * ```
 */
@Suppress("UNCHECKED_CAST")
public inline fun <reified T : Any, R : MsmpExtension> msmpExtension(
    registryName: String,
    crossinline factory: (MsmpClient, String, KSerializer<T>) -> R,
    crossinline config: MsmpExtensionConfig<R>.() -> Unit = {}
): ReadOnlyProperty<MsmpClient, R> = ReadOnlyProperty { thisRef, _ ->
    thisRef.callExtensions.getOrPut(registryName) {
        val extension = factory(thisRef, registryName, serializer<T>())
        MsmpExtensionConfig(extension).apply(config)
        extension
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
 * public val MsmpClient.server: ServerExtension
 *     by msmpExtension("minecraft:server", ::ServerExtension)
 * ```
 */
@Suppress("UNCHECKED_CAST")
public inline fun <R : MsmpExtension> msmpExtension(
    registryName: String,
    crossinline factory: (MsmpClient, String) -> R,
    crossinline config: MsmpExtensionConfig<R>.() -> Unit = {}
): ReadOnlyProperty<MsmpClient, R> = ReadOnlyProperty { thisRef, _ ->
    thisRef.callExtensions.getOrPut(registryName) {
        val extension = factory(thisRef, registryName)
        MsmpExtensionConfig(extension).apply(config)
        extension
    } as R
}

public interface MsmpExtension {
    public val client: MsmpClient
    public val baseEndpoint: String
}

public interface Syncable : MsmpExtension {
    public val flow: StateFlow<*>
}

public class MsmpExtensionConfig<T : MsmpExtension>(@PublishedApi internal val extension: T) {
    public inline fun <reified E : MsmpEvent> on(
        crossinline action: T.(E) -> Unit
    ): Job = extension.client.on<E> { extension.action(this) }

    public fun onConnection(block: suspend T.() -> Unit) {
        extension.client.stateFlow
            .filterIsInstance<MsmpState.Connected>()
            .onEach { extension.block() }
            .launchIn(extension.client.scope)
    }
}

public val MsmpClient.allowList: ArrayExtension<PlayerDto>
    by msmpExtension("minecraft:allowlist", ::ArrayExtension) {
        on<AllowlistAddedEvent> { evt -> cache.update { it.plus(evt.eventCtx) } }
        on<AllowlistRemovedEvent> { evt -> cache.update { it.minus(evt.eventCtx) } }

        onConnection { cache.update { get() } }
    }

public val MsmpClient.banList: ArrayExtension<UserBanDto>
    by msmpExtension("minecraft:bans", ::ArrayExtension) {
        on<UserBanAddedEvent> { evt -> cache.update { it.plus(evt.eventCtx) } }
        on<UserBanRemovedEvent> { evt -> cache.update { it.filterNot { ctx -> ctx.player == evt.eventCtx }.toSet() } }

        onConnection { cache.update { get() } }
    }

public val MsmpClient.ipBanList: ArrayExtension<IPBanDto>
    by msmpExtension("minecraft:ip_bans", ::ArrayExtension) {
        on<IPBanAddedEvent> { evt -> cache.update { it.plus(evt.eventCtx) } }
        on<IPBanRemovedEvent> { evt -> cache.update { it.filterNot { ctx -> ctx.ip == evt.eventCtx }.toSet() } }

        onConnection { cache.update { get() } }
    }

public val MsmpClient.operatorList: ArrayExtension<OperatorDto>
    by msmpExtension("minecraft:operators", ::ArrayExtension) {
        on<OperatorAddedEvent> { evt -> cache.update { it.plus(evt.eventCtx) } }
        on<OperatorRemovedEvent> { evt -> cache.update { it.minus(evt.eventCtx) } }

        onConnection { cache.update { get() } }
    }

public val MsmpClient.players: PlayersExtension
    by msmpExtension("minecraft:players", ::PlayersExtension) {
        on<PlayerJoinedEvent> { evt -> cache.update { it.plus(evt.eventCtx) } }
        on<PlayerLeftEvent> { evt -> cache.update { it.minus(evt.eventCtx) } }

        onConnection { cache.update { get() } }
    }

public val MsmpClient.gamerules: GamerulesExtension
    by msmpExtension("minecraft:gamerules", ::GamerulesExtension) {
        on<GameruleUpdatedEvent> { evt ->
            cache.update {
                it.filterNot { ctx -> ctx.key == evt.eventCtx.key }.toSet() + evt.eventCtx
            }
        }

        onConnection { cache.update { get() } }
    }

public val MsmpClient.server: ServerExtension
    by msmpExtension("minecraft:server", ::ServerExtension)

public val MsmpClient.serverSettings: ServerSettingsExtension
    by msmpExtension("minecraft:serversettings", ::ServerSettingsExtension)
