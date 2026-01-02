package tech.aliorpse.mcutils.api.extension

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.JsonElement
import tech.aliorpse.mcutils.api.MsmpClient
import tech.aliorpse.mcutils.api.registry.MsmpExtension
import tech.aliorpse.mcutils.api.registry.Syncable

public class ArrayExtension<T> @PublishedApi internal constructor(
    public override val client: MsmpClient,
    public override val baseEndpoint: String,
    public val serializer: KSerializer<T>
) : MsmpExtension, Syncable {
    internal val cache: MutableStateFlow<Set<T>> = MutableStateFlow(emptySet())

    public override val flow: StateFlow<Set<T>> = cache.asStateFlow()

    public suspend inline fun get(): Set<T> = decodeFrom(client.call(baseEndpoint))

    public suspend inline fun clear(): Set<T> =
        decodeFrom(client.call("$baseEndpoint/clear"))

    public suspend inline fun set(vararg value: T): Set<T> =
        decodeFrom(client.call("$baseEndpoint/set", listOf(value.toSet()), argsSerializer))

    public suspend inline fun add(vararg value: T): Set<T> =
        decodeFrom(client.call("$baseEndpoint/add", listOf(value.toSet()), argsSerializer))

    public suspend inline fun remove(vararg value: T): Set<T> =
        decodeFrom(client.call("$baseEndpoint/remove", listOf(value.toSet()), argsSerializer))

    // Using positional arguments here for universality, as parameter keys vary across different endpoints.
    @PublishedApi
    internal val argsSerializer: KSerializer<List<Set<T>>> = ListSerializer(SetSerializer(serializer))

    @PublishedApi
    internal fun decodeFrom(element: JsonElement): Set<T> =
        client.json.decodeFromJsonElement(SetSerializer(serializer), element)
}
