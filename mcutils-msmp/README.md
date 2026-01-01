# Minecraft Server Management Protocol (MSMP)

`tech.aliorpse.mcutils:mcutils-msmp:$version`

> [!tip]
> This module requires a Ktor client engine (e.g., `ktor-client-cio`).

## Common Usage

### Connection

```kotlin
val connection = MCServer.createMsmpConnection("ws://localhost:25585", "your_token")
connection.use { conn ->
    // Interact with the server
}

// Await connection to be closed
conn.await()
```

### Infrastructure API

#### Sending a request

```kotlin
val response: JsonElement = conn.call(
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

val response2: JsonElement = conn.call(
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
conn.eventFlow.filterIsInstance<PlayerJoinedEvent>().first().apply {
    println(eventCtx)
} // PlayerDto(name = ..., id = ...)

// Of course, you can launch a coroutine to handle events asynchronously
val job = GlobalScope.launch { conn.eventFlow.collect { println(it) } }
```

### High-level Built-In API

#### Sending a request

```kotlin
val players: Set<PlayerDto> = conn.allowList.add(PlayerDto(name = "Aliorpse"), PlayerDto(name = "MosCro"))

println(players)

conn.serverSettings.allowFlight.set(true)
conn.gamerules.set("send_command_feedback", true)

// And more...
```

#### Receiving an event

```kotlin
val job: Job = conn.on<PlayerJoinedEvent> { println(eventCtx.player) }

// Suspend until the event is received, then clean up the previous listener
conn.on<PlayerLeftEvent>().first().also { job.cancel() }
```

**Note**: The on function has multiple overloads. This demonstrates using both the Job-returning variant for persistent observation and the Flow-returning variant for one-time suspension.

## Customize

### Build your own High-level API

#### Request

```kotlin
public class GamerulesExtension internal constructor(
    public override val connection: MsmpConnection,
    public override val baseEndpoint: String
) : MsmpExtension {
    public suspend fun get(): Set<TypedGameruleDto> =
        Json.decodeFromJsonElement(
            SetSerializer(TypedGameruleDto.serializer()),
            connection.call(baseEndpoint)
        )

    public suspend inline fun set(gamerule: UntypedGameruleDto): TypedGameruleDto =
        Json.decodeFromJsonElement(
            TypedGameruleDto.serializer(),
            connection.call("$baseEndpoint/update", mapOf("gamerule" to gamerule))
        )
    
    // Other methods and overloads...
}

// Then register like this:
public val MsmpConnection.gamerules: GamerulesExtension
        by msmpExtension("minecraft:gamerules", ::GamerulesExtension)
```

Generic extensions are also supported via the reified delegate variant:

```kotlin
public class ArrayExtension<T> @PublishedApi internal constructor(
    public override val connection: MsmpConnection,
    public override val baseEndpoint: String,
    public val serializer: KSerializer<T>
) : MsmpExtension {

    public suspend inline fun set(vararg value: T): Set<T> =
        decodeFrom(connection.call("$baseEndpoint/set", listOf(value.toSet()), argsSerializer))
    
    @PublishedApi
    internal val argsSerializer: KSerializer<List<Set<T>>> = ListSerializer(SetSerializer(serializer))

    @PublishedApi
    internal fun decodeFrom(element: JsonElement): Set<T> =
        Json.decodeFromJsonElement(SetSerializer(serializer), element)

    // ...
}

// Same as above:
public val MsmpConnection.allowList: ArrayExtension<PlayerDto>
        by msmpExtension("minecraft:allowlist", ::ArrayExtension)

public val MsmpConnection.banList: ArrayExtension<UserBanDto>
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
conn.allowList.set(PlayerDto(name = "Aliorpse"))

conn.on<PlayerJoinedEvent> { println(eventCtx) }
```

For the full API reference, please refer to the project's [dokka](https://aliorpse.github.io/mcutils/msmp/).
