package tech.aliorpse.mcutils.api.extension

import tech.aliorpse.mcutils.api.MsmpClient
import tech.aliorpse.mcutils.api.registry.MsmpExtension
import tech.aliorpse.mcutils.entity.MessageDto
import tech.aliorpse.mcutils.entity.PlayerDto
import tech.aliorpse.mcutils.entity.ServerStateDto
import tech.aliorpse.mcutils.entity.SystemMessageDto

public class ServerExtension internal constructor(
    public override val client: MsmpClient,
    public override val baseEndpoint: String
) : MsmpExtension {
    public suspend inline fun status(): ServerStateDto =
        client.call("$baseEndpoint/status")

    public suspend inline fun save(flush: Boolean = false): Boolean =
        client.call("$baseEndpoint/save", mapOf("flush" to flush))

    public suspend inline fun stop(): Boolean =
        client.call("$baseEndpoint/stop")

    public suspend inline fun sendMessage(message: SystemMessageDto): Boolean =
        client.call("$baseEndpoint/system_message", mapOf("message" to message))

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
