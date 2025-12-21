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
    "players/joined" to EventProvider.Data(PlayerJoinedEvent.serializer()),
    "players/left" to EventProvider.Data(PlayerLeftEvent.serializer()),

    "operators/added" to EventProvider.Data(OperatorAddedEvent.serializer()),
    "operators/removed" to EventProvider.Data(OperatorRemovedEvent.serializer()),

    "allowlist/added" to EventProvider.Data(AllowlistAddedEvent.serializer()),
    "allowlist/removed" to EventProvider.Data(AllowlistRemovedEvent.serializer()),

    "ip_bans/added" to EventProvider.Data(IpBanAddedEvent.serializer()),
    "ip_bans/removed" to EventProvider.Custom { p ->
        IpBanRemovedEvent(p?.jsonPrimitive?.content ?: "")
    },
    "bans/added" to EventProvider.Data(UserBanAddedEvent.serializer()),
    "bans/removed" to EventProvider.Data(UserBanRemovedEvent.serializer()),

    "server/started" to EventProvider.Singleton(ServerStartedEvent),
    "server/stopping" to EventProvider.Singleton(ServerStoppingEvent),
    "server/activity" to EventProvider.Singleton(ServerActivityEvent),
    "server/saving" to EventProvider.Singleton(ServerSavingEvent),
    "server/saved" to EventProvider.Singleton(ServerSavedEvent),
    "server/status" to EventProvider.Data(ServerStatusEvent.serializer()),

    "gamerules/updated" to EventProvider.Data(GameruleUpdatedEvent.serializer())
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
public data class IpBanAddedEvent(val eventCtx: IpBanDto) : MsmpEvent

@Serializable
public data class IpBanRemovedEvent(val eventCtx: String) : MsmpEvent

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
public data class GameruleUpdatedEvent(val eventCtx: TypedRuleDto) : MsmpEvent

// Fallback

@Serializable
public data class UnknownMsmpEvent(
    val method: String,
    val params: JsonElement?
) : MsmpEvent
