package tech.aliorpse.mcutils.api.extension

import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import tech.aliorpse.mcutils.api.MsmpConnection
import tech.aliorpse.mcutils.api.registry.MsmpExtension

public class ServerSettingsExtension internal constructor(
    override val connection: MsmpConnection,
    override val baseEndpoint: String
) : MsmpExtension {
    public val autosave: BoolProp = BoolProp("autosave", "enable")
    public val enforceAllowlist: BoolProp = BoolProp("enforce_allowlist", "enforce")
    public val useAllowlist: BoolProp = BoolProp("use_allowlist", "use")
    public val allowFlight: BoolProp = BoolProp("allow_flight", "allow")
    public val forceGameMode: BoolProp = BoolProp("force_game_mode", "force")
    public val acceptTransfers: BoolProp = BoolProp("accept_transfers", "accept")
    public val hideOnlinePlayers: BoolProp = BoolProp("hide_online_players", "hide")
    public val statusReplies: BoolProp = BoolProp("status_replies", "enable")

    public val maxPlayers: IntProp = IntProp("max_players", "max")
    public val pauseWhenEmptySeconds: IntProp = IntProp("pause_when_empty_seconds", "seconds")
    public val playerIdleTimeout: IntProp = IntProp("player_idle_timeout", "seconds")
    public val spawnProtectionRadius: IntProp = IntProp("spawn_protection_radius", "radius")
    public val viewDistance: IntProp = IntProp("view_distance", "distance")
    public val simulationDistance: IntProp = IntProp("simulation_distance", "distance")
    public val statusHeartbeatInterval: IntProp = IntProp("status_heartbeat_interval", "seconds")
    public val operatorUserPermissionLevel: IntProp = IntProp("operator_user_permission_level", "level")
    public val entityBroadcastRange: IntProp = IntProp("entity_broadcast_range", "percentage_points")

    public val difficulty: StringProp = StringProp("difficulty", "difficulty")
    public val motd: StringProp = StringProp("motd", "message")
    public val gameMode: StringProp = StringProp("game_mode", "mode")

    public inner class BoolProp(@PublishedApi internal val path: String, @PublishedApi internal val param: String) {
        public suspend inline fun get(): Boolean =
            connection.call("$baseEndpoint/$path").jsonPrimitive.boolean
        public suspend inline fun set(value: Boolean): Boolean =
            connection.call("$baseEndpoint/$path/set", mapOf(param to value)).jsonPrimitive.boolean
    }

    public inner class IntProp(@PublishedApi internal val path: String, @PublishedApi internal val param: String) {
        public suspend inline fun get(): Int =
            connection.call("$baseEndpoint/$path").jsonPrimitive.int
        public suspend inline fun set(value: Int): Int =
            connection.call("$baseEndpoint/$path/set", mapOf(param to value)).jsonPrimitive.int
    }

    public inner class StringProp(@PublishedApi internal val path: String, @PublishedApi internal val param: String) {
        public suspend inline fun get(): String =
            connection.call("$baseEndpoint/$path").jsonPrimitive.content
        public suspend inline fun set(value: String): String =
            connection.call("$baseEndpoint/$path/set", mapOf(param to value)).jsonPrimitive.content
    }
}
