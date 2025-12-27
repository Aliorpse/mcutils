package tech.aliorpse.mcutils.api.registry

import kotlinx.serialization.json.jsonPrimitive
import tech.aliorpse.mcutils.entity.AllowlistAddedEvent
import tech.aliorpse.mcutils.entity.AllowlistRemovedEvent
import tech.aliorpse.mcutils.entity.GameruleUpdatedEvent
import tech.aliorpse.mcutils.entity.IMsmpEventRegistry
import tech.aliorpse.mcutils.entity.IPBanAddedEvent
import tech.aliorpse.mcutils.entity.IPBanRemovedEvent
import tech.aliorpse.mcutils.entity.MsmpEventRegistryImpl
import tech.aliorpse.mcutils.entity.OperatorAddedEvent
import tech.aliorpse.mcutils.entity.OperatorRemovedEvent
import tech.aliorpse.mcutils.entity.PlayerJoinedEvent
import tech.aliorpse.mcutils.entity.PlayerLeftEvent
import tech.aliorpse.mcutils.entity.ServerActivityEvent
import tech.aliorpse.mcutils.entity.ServerSavedEvent
import tech.aliorpse.mcutils.entity.ServerSavingEvent
import tech.aliorpse.mcutils.entity.ServerStartedEvent
import tech.aliorpse.mcutils.entity.ServerStatusEvent
import tech.aliorpse.mcutils.entity.ServerStoppingEvent
import tech.aliorpse.mcutils.entity.UserBanAddedEvent
import tech.aliorpse.mcutils.entity.UserBanRemovedEvent
import tech.aliorpse.mcutils.internal.util.AtomicCopyOnWriteMap

/**
 * Registry for MSMP events.
 *
 * ```kotlin
 * @Serializable
 * public data class PlayerJoinedEvent(val eventCtx: PlayerDto) : MsmpEvent
 *
 * public data object ServerStartedEvent : MsmpEvent
 *
 * @Serializable
 * public data class IPBanRemovedEvent(val eventCtx: String) : MsmpEvent
 *
 * MsmpEventRegistry.configure {
 *     "minecraft:notification/players/joined" register PlayerJoinedEvent.serializer()
 *
 *     "minecraft:notification/server/started" bind ServerStartedEvent
 *
 *     "minecraft:notification/ip_bans/removed".define { p ->
 *         IPBanRemovedEvent(p?.jsonPrimitive?.content ?: "")
 *     }
 * }
 * ```
 */
public object MsmpEventRegistry {
    @PublishedApi
    internal val impl: MsmpEventRegistryImpl = MsmpEventRegistryImpl(AtomicCopyOnWriteMap())

    public fun configure(block: IMsmpEventRegistry.() -> Unit): Unit = impl.block()

    init {
        configure {
            "minecraft:notification/players/joined" register PlayerJoinedEvent.serializer()
            "minecraft:notification/players/left" register PlayerLeftEvent.serializer()

            "minecraft:notification/operators/added" register OperatorAddedEvent.serializer()
            "minecraft:notification/operators/removed" register OperatorRemovedEvent.serializer()

            "minecraft:notification/allowlist/added" register AllowlistAddedEvent.serializer()
            "minecraft:notification/allowlist/removed" register AllowlistRemovedEvent.serializer()

            "minecraft:notification/ip_bans/added" register IPBanAddedEvent.serializer()
            "minecraft:notification/ip_bans/removed".define { p ->
                IPBanRemovedEvent(p?.jsonPrimitive?.content ?: "")
            }

            "minecraft:notification/bans/added" register UserBanAddedEvent.serializer()
            "minecraft:notification/bans/removed" register UserBanRemovedEvent.serializer()

            "minecraft:notification/server/started" bind ServerStartedEvent
            "minecraft:notification/server/stopping" bind ServerStoppingEvent
            "minecraft:notification/server/activity" bind ServerActivityEvent
            "minecraft:notification/server/saving" bind ServerSavingEvent
            "minecraft:notification/server/saved" bind ServerSavedEvent
            "minecraft:notification/server/status" register ServerStatusEvent.serializer()

            "minecraft:notification/gamerules/updated" register GameruleUpdatedEvent.serializer()
        }
    }
}
