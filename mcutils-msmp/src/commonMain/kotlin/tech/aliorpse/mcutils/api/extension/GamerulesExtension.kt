package tech.aliorpse.mcutils.api.extension

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonPrimitive
import tech.aliorpse.mcutils.api.MsmpClient
import tech.aliorpse.mcutils.api.registry.MsmpExtension
import tech.aliorpse.mcutils.api.registry.Syncable
import tech.aliorpse.mcutils.entity.TypedGameruleDto
import tech.aliorpse.mcutils.entity.UntypedGameruleDto

public class GamerulesExtension internal constructor(
    public override val client: MsmpClient,
    public override val baseEndpoint: String
) : MsmpExtension, Syncable {
    internal val cache: MutableStateFlow<Set<TypedGameruleDto>> = MutableStateFlow(emptySet())

    public override val flow: StateFlow<Set<TypedGameruleDto>> = cache.asStateFlow()

    public fun snapshot(): Set<TypedGameruleDto> = cache.value

    public suspend inline fun set(gamerule: UntypedGameruleDto): TypedGameruleDto =
        client.json.decodeFromJsonElement(
            TypedGameruleDto.serializer(),
            client.call("$baseEndpoint/update", mapOf("gamerule" to gamerule))
        )

    public suspend inline fun set(gamerule: String, value: Boolean): TypedGameruleDto =
        set(UntypedGameruleDto(gamerule, JsonPrimitive(value)))

    public suspend inline fun set(gamerule: String, value: Int): TypedGameruleDto =
        set(UntypedGameruleDto(gamerule, JsonPrimitive(value)))
}
