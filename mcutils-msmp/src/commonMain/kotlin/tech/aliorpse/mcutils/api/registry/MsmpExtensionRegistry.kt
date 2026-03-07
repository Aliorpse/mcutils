package tech.aliorpse.mcutils.api.registry

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import tech.aliorpse.mcutils.api.MsmpClient
import tech.aliorpse.mcutils.api.MsmpState
import tech.aliorpse.mcutils.api.extension.*
import tech.aliorpse.mcutils.entity.*
import kotlin.properties.ReadOnlyProperty

/**
 * Registry for MSMP request extension.
 *
 * This variant automatically injects [registryName] and the [KSerializer] for [P] and [R] into the [factory].
 *
 * Example:
 * ```kotlin
 * public val MsmpClient.allowList: ArrayExtension<PlayerDto>
 *     by msmpExtension("minecraft:allowlist", ::ArrayExtension)
 * ```
 */
@Suppress("UNCHECKED_CAST")
public inline fun <reified P : Any, reified R : Any, T : MsmpExtension> msmpExtension(
    registryName: String,
    crossinline factory: (MsmpClient, String, KSerializer<P>, KSerializer<R>) -> T,
    crossinline config: MsmpExtensionConfig<T>.() -> Unit = {}
): ReadOnlyProperty<MsmpClient, T> = ReadOnlyProperty { thisRef, _ ->
    thisRef.callExtensions.getOrPut(registryName) {
        val extension = factory(thisRef, registryName, serializer<P>(), serializer<R>())
        MsmpExtensionConfig(extension).apply(config)
        extension
    } as T
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
public inline fun <T : MsmpExtension> msmpExtension(
    registryName: String,
    crossinline factory: (MsmpClient, String) -> T,
    crossinline config: MsmpExtensionConfig<T>.() -> Unit = {}
): ReadOnlyProperty<MsmpClient, T> = ReadOnlyProperty { thisRef, _ ->
    thisRef.callExtensions.getOrPut(registryName) {
        val extension = factory(thisRef, registryName)
        MsmpExtensionConfig(extension).apply(config)
        extension
    } as T
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
