package tech.aliorpse.mcutils.api.extension

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.JsonElement
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

    public fun snapshot(): Set<PlayerDto> = cache.value

    public suspend inline fun kick(vararg player: KickPlayerDto): Set<PlayerDto> =
        decodeFrom(client.call("$baseEndpoint/kick", player.toSet()))

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
        return decodeFrom(client.call("$baseEndpoint/kick", kickList.toSet()))
    }

    @PublishedApi
    internal fun decodeFrom(element: JsonElement): Set<PlayerDto> =
        client.json.decodeFromJsonElement(
            SetSerializer(PlayerDto.serializer()),
            element
        )
}
