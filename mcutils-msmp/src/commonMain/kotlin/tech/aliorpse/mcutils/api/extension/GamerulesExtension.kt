package tech.aliorpse.mcutils.api.extension

import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.JsonPrimitive
import tech.aliorpse.mcutils.api.MsmpConnection
import tech.aliorpse.mcutils.entity.TypedGameruleDto
import tech.aliorpse.mcutils.entity.UntypedGameruleDto

public class GamerulesExtension(public val connection: MsmpConnection) {
    @PublishedApi
    internal val baseEndpoint: String = "minecraft:gamerules"

    public suspend inline fun get(): Set<TypedGameruleDto> =
        connection.impl.json.decodeFromJsonElement(
            SetSerializer(TypedGameruleDto.serializer()),
            connection.call(baseEndpoint)
        )

    public suspend inline fun set(gamerule: UntypedGameruleDto): TypedGameruleDto =
        connection.impl.json.decodeFromJsonElement(
            TypedGameruleDto.serializer(),
            connection.call("$baseEndpoint/update", mapOf("gamerule" to gamerule))
        )

    public suspend inline fun set(gamerule: String, value: Boolean): TypedGameruleDto =
        set(UntypedGameruleDto(gamerule, JsonPrimitive(value)))

    public suspend inline fun set(gamerule: String, value: Int): TypedGameruleDto =
        set(UntypedGameruleDto(gamerule, JsonPrimitive(value)))
}
