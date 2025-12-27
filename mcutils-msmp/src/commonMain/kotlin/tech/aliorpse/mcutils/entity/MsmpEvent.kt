package tech.aliorpse.mcutils.entity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import tech.aliorpse.mcutils.api.registry.MsmpEventRegistry
import tech.aliorpse.mcutils.internal.util.AtomicCopyOnWriteMap

public interface IMsmpEventRegistry {
    public fun <T : MsmpEvent> registerData(method: String, serializer: KSerializer<T>)
    public fun registerSingleton(method: String, instance: MsmpEvent)
    public fun registerCustom(method: String, block: (JsonElement?) -> MsmpEvent)

    public infix fun <T : MsmpEvent> String.register(serializer: KSerializer<T>): Unit =
        registerData(this, serializer)

    public infix fun String.bind(instance: MsmpEvent): Unit =
        registerSingleton(this, instance)

    public fun String.define(block: (JsonElement?) -> MsmpEvent): Unit =
        registerCustom(this, block)
}

public sealed class MsmpEventProvider {
    public class Data<T : MsmpEvent>(public val serializer: KSerializer<T>) : MsmpEventProvider()
    public class Singleton(public val instance: MsmpEvent) : MsmpEventProvider()
    public class Custom(public val block: (JsonElement?) -> MsmpEvent) : MsmpEventProvider()
}

internal class MsmpEventRegistryImpl(
    private val map: AtomicCopyOnWriteMap<String, MsmpEventProvider>
) : IMsmpEventRegistry {
    val registry: AtomicCopyOnWriteMap<String, MsmpEventProvider> get() = map

    override fun <T : MsmpEvent> registerData(method: String, serializer: KSerializer<T>) =
        map.put(method, MsmpEventProvider.Data(serializer))

    override fun registerSingleton(method: String, instance: MsmpEvent) =
        map.put(method, MsmpEventProvider.Singleton(instance))

    override fun registerCustom(method: String, block: (JsonElement?) -> MsmpEvent) =
        map.put(method, MsmpEventProvider.Custom(block))
}

internal val eventMap: AtomicCopyOnWriteMap<String, MsmpEventProvider> = MsmpEventRegistry.impl.registry

@Serializable
public sealed interface MsmpEvent

// Connection-related Events

public data object ConnectionEstablishedEvent : MsmpEvent

public data class ConnectionClosedEvent(val cause: String) : MsmpEvent

// Players Events

@Serializable
public data class PlayerJoinedEvent(val eventCtx: PlayerDto) : MsmpEvent

@Serializable
public data class PlayerLeftEvent(val eventCtx: PlayerDto) : MsmpEvent

// Operator Events

@Serializable
public data class OperatorAddedEvent(val eventCtx: OperatorDto) : MsmpEvent

@Serializable
public data class OperatorRemovedEvent(val eventCtx: OperatorDto) : MsmpEvent

// Allowlist Events

@Serializable
public data class AllowlistAddedEvent(val eventCtx: PlayerDto) : MsmpEvent

@Serializable
public data class AllowlistRemovedEvent(val eventCtx: PlayerDto) : MsmpEvent

// Ban Events

@Serializable
public data class IPBanAddedEvent(val eventCtx: IPBanDto) : MsmpEvent

@Serializable
public data class IPBanRemovedEvent(val eventCtx: String) : MsmpEvent

@Serializable
public data class UserBanAddedEvent(val eventCtx: UserBanDto) : MsmpEvent

@Serializable
public data class UserBanRemovedEvent(val eventCtx: PlayerDto) : MsmpEvent

// Server State Events

public data object ServerStartedEvent : MsmpEvent

public data object ServerStoppingEvent : MsmpEvent

public data object ServerActivityEvent : MsmpEvent

public data object ServerSavingEvent : MsmpEvent

public data object ServerSavedEvent : MsmpEvent

@Serializable
public data class ServerStatusEvent(val eventCtx: ServerStateDto) : MsmpEvent

// Gamerule Events

@Serializable
public data class GameruleUpdatedEvent(val eventCtx: TypedGameruleDto) : MsmpEvent

// Fallback

@Serializable
public data class UnknownMsmpEvent(
    val method: String,
    val params: JsonElement?
) : MsmpEvent
