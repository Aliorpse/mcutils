package tech.aliorpse.mcutils.entity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

internal sealed class EventProvider {
    class Data<T : MsmpEvent>(val serializer: KSerializer<T>) : EventProvider()
    class Singleton(val instance: MsmpEvent) : EventProvider()
    class Custom(val block: (JsonElement?) -> MsmpEvent) : EventProvider()
}

internal val eventMap: Map<String, EventProvider> = mapOf(
    "minecraft:notification/players/joined" to EventProvider.Data(PlayerJoinedEvent.serializer()),
    "minecraft:notification/players/left" to EventProvider.Data(PlayerLeftEvent.serializer()),

    "minecraft:notification/operators/added" to EventProvider.Data(OperatorAddedEvent.serializer()),
    "minecraft:notification/operators/removed" to EventProvider.Data(OperatorRemovedEvent.serializer()),

    "minecraft:notification/allowlist/added" to EventProvider.Data(AllowlistAddedEvent.serializer()),
    "minecraft:notification/allowlist/removed" to EventProvider.Data(AllowlistRemovedEvent.serializer()),

    "minecraft:notification/ip_bans/added" to EventProvider.Data(IPBanAddedEvent.serializer()),
    "minecraft:notification/ip_bans/removed" to EventProvider.Custom { p ->
        IPBanRemovedEvent(p?.jsonPrimitive?.content ?: "")
    },
    "minecraft:notification/bans/added" to EventProvider.Data(UserBanAddedEvent.serializer()),
    "minecraft:notification/bans/removed" to EventProvider.Data(UserBanRemovedEvent.serializer()),

    "minecraft:notification/server/started" to EventProvider.Singleton(ServerStartedEvent),
    "minecraft:notification/server/stopping" to EventProvider.Singleton(ServerStoppingEvent),
    "minecraft:notification/server/activity" to EventProvider.Singleton(ServerActivityEvent),
    "minecraft:notification/server/saving" to EventProvider.Singleton(ServerSavingEvent),
    "minecraft:notification/server/saved" to EventProvider.Singleton(ServerSavedEvent),
    "minecraft:notification/server/status" to EventProvider.Data(ServerStatusEvent.serializer()),

    "minecraft:notification/gamerules/updated" to EventProvider.Data(GameruleUpdatedEvent.serializer())
)

@Serializable
public sealed interface MsmpEvent

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

@Serializable
public data object ServerStartedEvent : MsmpEvent

@Serializable
public data object ServerStoppingEvent : MsmpEvent

@Serializable
public data object ServerActivityEvent : MsmpEvent

@Serializable
public data object ServerSavingEvent : MsmpEvent

@Serializable
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
