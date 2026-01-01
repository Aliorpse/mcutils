package tech.aliorpse.mcutils.api.extension

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.JsonElement
import tech.aliorpse.mcutils.api.MsmpConnection
import tech.aliorpse.mcutils.api.registry.MsmpExtension

public class ArrayExtension<T> @PublishedApi internal constructor(
    public override val connection: MsmpConnection,
    public override val baseEndpoint: String,
    public val serializer: KSerializer<T>
) : MsmpExtension {
    public suspend inline fun get(): Set<T> =
        decodeFrom(connection.call(baseEndpoint))

    public suspend inline fun clear(): Set<T> =
        decodeFrom(connection.call("$baseEndpoint/clear"))

    public suspend inline fun set(vararg value: T): Set<T> =
        decodeFrom(connection.call("$baseEndpoint/set", listOf(value.toSet()), argsSerializer))

    public suspend inline fun add(vararg value: T): Set<T> =
        decodeFrom(connection.call("$baseEndpoint/add", listOf(value.toSet()), argsSerializer))

    public suspend inline fun remove(vararg value: T): Set<T> =
        decodeFrom(connection.call("$baseEndpoint/remove", listOf(value.toSet()), argsSerializer))

    // Using positional arguments here for universality, as parameter keys vary across different endpoints.
    @PublishedApi
    internal val argsSerializer: KSerializer<List<Set<T>>> = ListSerializer(SetSerializer(serializer))

    @PublishedApi
    internal fun decodeFrom(element: JsonElement): Set<T> =
        connection.impl.json.decodeFromJsonElement(SetSerializer(serializer), element)
}
