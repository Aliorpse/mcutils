package tech.aliorpse.mcutils.api.extension

import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import tech.aliorpse.mcutils.api.MsmpConnection
import tech.aliorpse.mcutils.entity.MessageDto
import tech.aliorpse.mcutils.entity.PlayerDto
import tech.aliorpse.mcutils.entity.ServerStateDto
import tech.aliorpse.mcutils.entity.SystemMessageDto

public class ServerExtension(public val connection: MsmpConnection) {
    @PublishedApi
    internal val baseEndpoint: String = "minecraft:server"

    public suspend inline fun status(): ServerStateDto =
        connection.impl.json.decodeFromJsonElement(
            ServerStateDto.serializer(),
            connection.call("$baseEndpoint/status")
        )

    public suspend inline fun save(flush: Boolean = false): Boolean {
        val result = connection.call("$baseEndpoint/save", mapOf("flush" to flush))
        return result.jsonPrimitive.boolean
    }

    public suspend inline fun stop(): Boolean {
        val result = connection.call("$baseEndpoint/stop")
        return result.jsonPrimitive.boolean
    }

    public suspend inline fun sendMessage(message: SystemMessageDto): Boolean {
        val result = connection.call("$baseEndpoint/system_message", mapOf("message" to message))
        return result.jsonPrimitive.boolean
    }

    public suspend inline fun sendMessage(
        targets: Set<PlayerDto>,
        message: MessageDto,
        overlay: Boolean = false
    ): Boolean = sendMessage(SystemMessageDto(targets, message, overlay))

    /**
     * Send a given literal message to the given players by their name.
     */
    public suspend inline fun sendMessage(
        vararg target: String,
        message: String,
        overlay: Boolean = false
    ): Boolean = sendMessage(
        SystemMessageDto(
            receivingPlayers = target.map { PlayerDto(name = it) }.toSet(),
            message = MessageDto(literal = message),
            overlay = overlay
        )
    )
}
