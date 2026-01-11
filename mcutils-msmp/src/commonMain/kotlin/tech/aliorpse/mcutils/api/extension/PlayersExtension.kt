package tech.aliorpse.mcutils.api.extension

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.aliorpse.mcutils.api.MsmpClient
import tech.aliorpse.mcutils.api.registry.MsmpExtension
import tech.aliorpse.mcutils.api.registry.Syncable
import tech.aliorpse.mcutils.entity.KickPlayerDto
import tech.aliorpse.mcutils.entity.MessageDto
import tech.aliorpse.mcutils.entity.PlayerDto

public class PlayersExtension internal constructor(
    public override val client: MsmpClient,
    public override val baseEndpoint: String
) : MsmpExtension, Syncable {
    internal val cache: MutableStateFlow<Set<PlayerDto>> = MutableStateFlow(emptySet())

    public override val flow: StateFlow<Set<PlayerDto>> = cache.asStateFlow()

    public suspend inline fun get(): Set<PlayerDto> =
        client.call(baseEndpoint)

    public suspend inline fun kick(vararg player: KickPlayerDto): Set<PlayerDto> =
        client.call("$baseEndpoint/kick", player.toSet())

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

        return kick(*kickList.toTypedArray())
    }
}
