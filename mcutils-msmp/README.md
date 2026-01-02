# Minecraft Server Management Protocol (MSMP)

`tech.aliorpse.mcutils:mcutils-msmp:$version`

> [!tip]
> This module requires a Ktor client engine (e.g., `ktor-client-cio`).

## Common Usage

### Connection

```kotlin
val client = MCServer.msmpClient("ws://localhost:25585", "your_token") {
    // Optional configuration
    autoReconnect = true
    maxReconnectAttempts = 5
    failFast = true // Stop reconnecting if the first attempt fails
    batchDelay = 30 // Group concurrent requests (ms)
}

client.use { client ->
    client.startConnection()

    // Interact with the server
    client.call("...")

    // Await client to be closed (e.g., server stopping or manual close)
    client.await()
}
```

#### Connection Behavior

- **Automatic Reconnection**: Enabled by default. The client uses an exponential backoff strategy to reconnect if the connection is lost. Use `failFast` to stop if the initial connection fails.
- **Request Buffering**: `call()` and other API methods will **suspend** if the client is connecting or reconnecting, waiting for a valid connection.
- **Request Batching**: Concurrent requests are automatically coalesced into JSON-RPC batches based on `batchDelay`.

```kotlin
// The `call` function will wait for `requestTimeout`
client.startConnection()
client.call("...")

// The `call` will wait for connection and then `requestTimeout`
client.startConnection()
client.awaitState<MsmpState.Connected>()
client.call("...")
```

- **Lifecycle**: `client.on<T> { ... }` (Job-returning) listeners are bound to the client's internal scope. They remain active during reconnection and are canceled when the client is closed.


### Infrastructure API

#### Sending a request

```kotlin
val response: JsonElement = client.call(
    "minecraft:players/kick",
    // Passing arguments by position
    setOf(
        setOf(
            KickPlayerDto(
                PlayerDto(name = "Aliorpse"),
                MessageDto(literal = "Kicked by server")
            )
        )
    )
)

val response2: JsonElement = client.call(
    "minecraft:players/kick",
    // Passing arguments by name
    mapOf(
        "kick" to setOf(
            KickPlayerDto(
                PlayerDto(name = "Aliorpse"),
                MessageDto(literal = "Kicked by server")
            )
        )
    )
)

println(
    Json.decodeFromJsonElement(
        SetSerializer(PlayerDto.serializer()),
        response
    )
)
```

#### Receiving an event

```kotlin
// Suspend until event is received
client.eventFlow.filterIsInstance<PlayerJoinedEvent>().first().apply {
    println(eventCtx)
} // PlayerDto(name = ..., id = ...)

// Of course, you can launch a coroutine to handle events asynchronously
val job = GlobalScope.launch { client.eventFlow.collect { println(it) } }
```

### High-level Built-In API

#### Sending a request

```kotlin
val players: Set<PlayerDto> = client.allowList.add(PlayerDto(name = "Aliorpse"), PlayerDto(name = "MosCro"))

println(players)

client.serverSettings.allowFlight.set(true)
client.gamerules.set("send_command_feedback", true)

// And more...
```

#### Receiving an event

```kotlin
val job: Job = client.on<PlayerJoinedEvent> { println(eventCtx.player) }

// Suspend until the event is received, then clean up the previous listener
client.on<PlayerLeftEvent>().first().also { job.cancel() }

val event = client.awaitEvent<OperatorAddedEvent>()
println(event.eventCtx.player.name)

val state = client.awaitState<MsmpState.Reconnecting>()
println(state.attempt, state.nextDelay)
```

**Note**: The on function has multiple overloads. This demonstrates using both the Job-returning variant for persistent observation and the Flow-returning variant for one-time suspension.

## Advanced Features

### Request Batching
To optimize network usage, the client can group multiple requests into a single JSON-RPC batch within a small time window. This is highly effective for reducing overhead during high-concurrency operations.

```kotlin
val client = MCServer.msmpClient("...") {
    // Group requests occurring within 30ms (default)
    batchDelay = 30
}
```

### State Synchronization
Most built-in extensions (like `players`, `gamerules`, `allowList`, etc.) implement the `Syncable` interface. They maintain an internal cache automatically synchronized with the server via events.

```kotlin
// Get an immediate snapshot of the current state without network calls
val onlinePlayers = client.players.snapshot()

// Or observe the state reactively using a Flow
client.players.flow.collect { players ->
    println("Currently online: ${players.size}")
}
```

## Customize

### Build your own High-level API

#### Request

```kotlin
public class GamerulesExtension internal constructor(
    public override val client: MsmpClient,
    public override val baseEndpoint: String
) : MsmpExtension {
    public suspend fun get(): Set<TypedGameruleDto> =
        client.json.decodeFromJsonElement(
            SetSerializer(TypedGameruleDto.serializer()),
            client.call(baseEndpoint)
        )

    public suspend inline fun set(gamerule: UntypedGameruleDto): TypedGameruleDto =
        client.json.decodeFromJsonElement(
            TypedGameruleDto.serializer(),
            client.call("$baseEndpoint/update", mapOf("gamerule" to gamerule))
        )
    
    // Other methods and overloads...
}

// Then register like this:
public val MsmpClient.gamerules: GamerulesExtension
    by msmpExtension("minecraft:gamerules", ::GamerulesExtension) {
        // Optional: set up automatic synchronization
        on<GameruleUpdatedEvent> { evt ->
            // Assume 'cache' is a MutableStateFlow in your extension
            cache.update { it.filterNot { ctx -> ctx.key == evt.eventCtx.key }.toSet() + evt.eventCtx }
        }
        
        // Refresh cache on every successful connection
        onConnection { cache.update { yourFunction() } }
    }
```

Generic extensions are also supported via the reified delegate variant:

```kotlin
public class ArrayExtension<T> @PublishedApi internal constructor(
    public override val client: MsmpClient,
    public override val baseEndpoint: String,
    public val serializer: KSerializer<T>
) : MsmpExtension, Syncable {
    internal val cache = MutableStateFlow<Set<T>>(emptySet())
    public override val flow: StateFlow<Set<T>> = cache.asStateFlow()

    public fun snapshot(): Set<T> = cache.value

    public suspend inline fun set(vararg value: T): Set<T> =
        decodeFrom(client.call("$baseEndpoint/set", listOf(value.toSet()), argsSerializer))
    
    @PublishedApi
    internal val argsSerializer: KSerializer<List<Set<T>>> = ListSerializer(SetSerializer(serializer))

    @PublishedApi
    internal fun decodeFrom(element: JsonElement): Set<T> =
        client.json.decodeFromJsonElement(SetSerializer(serializer), element)

    // ...
}

// Same as above:
public val MsmpClient.allowList: ArrayExtension<PlayerDto>
    by msmpExtension("minecraft:allowlist", ::ArrayExtension)

public val MsmpClient.banList: ArrayExtension<UserBanDto>
    by msmpExtension("minecraft:bans", ::ArrayExtension)
```

**Note**: It is highly recommended to use inline for your extension methods. The library is internally designed to minimize code duplication.

#### Event

```kotlin
@Serializable
public data class PlayerJoinedEvent(val eventCtx: PlayerDto) : MsmpEvent

public data object ServerStartedEvent : MsmpEvent

public data class IPBanRemovedEvent(val eventCtx: String) : MsmpEvent

// Then register them:
MsmpEventRegistry.configure {
    // Map JSON to a data class
    "minecraft:notification/players/joined" register PlayerJoinedEvent.serializer()

    // For events without a body
    "minecraft:notification/server/started" bind ServerStartedEvent

    // Manual parsing for primitive or complex logic
    "minecraft:notification/ip_bans/removed".define { IPBanRemovedEvent(it?.jsonPrimitive?.content ?: "") }
}
```

### Usage

Now feel free to use them like the built-in ones.

```kotlin
client.allowList.set(PlayerDto(name = "Aliorpse"))

client.on<PlayerJoinedEvent> { println(eventCtx) }
```

For the full API reference, please refer to the project's [dokka](https://aliorpse.github.io/mcutils/msmp/).
