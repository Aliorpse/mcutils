package tech.aliorpse.mcutils.api.extension

import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.JsonElement
import tech.aliorpse.mcutils.api.MsmpConnection
import tech.aliorpse.mcutils.entity.KickPlayerDto
import tech.aliorpse.mcutils.entity.MessageDto
import tech.aliorpse.mcutils.entity.PlayerDto

public class PlayersExtension(public val connection: MsmpConnection) {
    @PublishedApi
    internal val baseEndpoint: String = "minecraft:players"

    /**
     * Get a list of all players on the server.
     */
    public suspend inline fun get(): Set<PlayerDto> =
        decodeFrom(connection.call(baseEndpoint))

    /**
     * Kick the given players.
     */
    public suspend inline fun kick(vararg player: KickPlayerDto): Set<PlayerDto> =
        decodeFrom(connection.call("$baseEndpoint/kick", player.toSet()))

    /**
     * Kick the given players by their name, and message in literal.
     */
    public suspend inline fun kick(vararg player: String, message: String = ""): Set<PlayerDto> {
        val kickList = player.map { name ->
            KickPlayerDto(
                player = PlayerDto(name = name),
                message = MessageDto(literal = message)
            )
        }
        return decodeFrom(connection.call("$baseEndpoint/kick", kickList.toSet()))
    }

    @PublishedApi
    internal fun decodeFrom(element: JsonElement): Set<PlayerDto> =
        connection.impl.json.decodeFromJsonElement(
            SetSerializer(PlayerDto.serializer()),
            element
        )
}
