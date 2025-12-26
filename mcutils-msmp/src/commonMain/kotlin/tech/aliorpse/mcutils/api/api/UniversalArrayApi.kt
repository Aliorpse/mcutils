package tech.aliorpse.mcutils.api.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import tech.aliorpse.mcutils.api.MsmpConnection

// Using positional arguments here for universality, as parameter keys vary across different endpoints.
public class UniversalArrayApi<T>(
    public val connection: MsmpConnection,
    public val baseEndpoint: String,
    public val serializer: KSerializer<T>
) {
    public suspend inline fun get(): Set<T> =
        decodeFrom(connection.call(baseEndpoint))

    public suspend inline fun clear(): Set<T> =
        decodeFrom(connection.call("$baseEndpoint/clear"))

    public suspend inline fun set(vararg value: T): Set<T> =
        decodeFrom(connection.call("$baseEndpoint/set", serializeArgs(value)))

    public suspend inline fun add(vararg value: T): Set<T> =
        decodeFrom(connection.call("$baseEndpoint/add", serializeArgs(value)))

    public suspend inline fun remove(vararg value: T): Set<T> =
        decodeFrom(connection.call("$baseEndpoint/remove", serializeArgs(value)))

    @PublishedApi
    internal fun serializeArgs(value: Array<out T>): JsonArray {
        val jsonElement = connection.impl.json.encodeToJsonElement(
            SetSerializer(serializer),
            value.toSet()
        )
        return JsonArray(listOf(jsonElement))
    }

    @PublishedApi
    internal fun decodeFrom(element: JsonElement): Set<T> =
        connection.impl.json.decodeFromJsonElement(SetSerializer(serializer), element)
}
