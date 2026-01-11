package tech.aliorpse.mcutils.api.extension

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import tech.aliorpse.mcutils.api.MsmpClient
import tech.aliorpse.mcutils.api.registry.MsmpExtension
import tech.aliorpse.mcutils.api.registry.Syncable

public class ArrayExtension<T> @PublishedApi internal constructor(
    public override val client: MsmpClient,
    public override val baseEndpoint: String,
    paramSerializer: KSerializer<T>,
    responseSerializer: KSerializer<T>
) : MsmpExtension, Syncable {
    internal val cache: MutableStateFlow<Set<T>> = MutableStateFlow(emptySet())

    public override val flow: StateFlow<Set<T>> = cache.asStateFlow()

    public suspend inline fun get(): Set<T> =
        client.call(baseEndpoint, responseSerializer)

    public suspend inline fun clear(): Set<T> =
        client.call("$baseEndpoint/clear", responseSerializer)

    public suspend inline fun set(vararg value: T): Set<T> =
        client.call("$baseEndpoint/set", listOf(value.toSet()), paramSerializer, responseSerializer)

    public suspend inline fun add(vararg value: T): Set<T> =
        client.call("$baseEndpoint/add", listOf(value.toSet()), paramSerializer, responseSerializer)

    public suspend inline fun remove(vararg value: T): Set<T> =
        client.call("$baseEndpoint/remove", listOf(value.toSet()), paramSerializer, responseSerializer)

    // Using positional arguments here for universality, as parameter keys vary across different endpoints.
    @PublishedApi
    internal val paramSerializer: KSerializer<List<Set<T>>> = ListSerializer(SetSerializer(paramSerializer))

    @PublishedApi
    internal val responseSerializer: KSerializer<Set<T>> = SetSerializer(responseSerializer)
}
