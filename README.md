# mcutils

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Aliorpse_mcutils&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Aliorpse_mcutils)
[![Maven Central](https://maven-badges.sml.io/sonatype-central/tech.aliorpse.mcutils/mcutils-core/badge.svg)](https://central.sonatype.com/artifact/tech.aliorpse.mcutils/mcutils-core)
[![View on DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Aliorpse/mcutils)

A Kotlin multiplatform library provides utility functions for Minecraft-related queries.

## Modules

### Server Status

`tech.aliorpse.mcutils:mcutils-server-status:$version`

> [!warning]
> Native targets do not yet support an SRV record implementation. Setting `enableSrv = true` won't have any effect.

```kotlin
runBlocking {
    val status1 = MCServer.getStatus("mc.hypixel.net")
    val status2 = MCServer.getStatus(host = "wdsj.net", port = 25565, enableSrv = false)

    val queryFull = MCServer.getQueryFull("mc.hypixel.net")
    val queryBasic = MCServer.getQueryBasic("mc.hypixel.net")
}
```

### Server Management Protocol

`tech.aliorpse.mcutils:mcutils-msmp:$version`

#### Common Usage

```kotlin
runBlocking {
    MCServer.createMsmpConnection("ws://localhost:25585", "xxx")
        .use { conn ->
            GlobalScope.launch { conn.eventFlow.collect { println(it) } }

            val job = conn.on<PlayerJoinedEvent> {
                println(eventCtx.name)
            }

            conn.on<PlayerLeftEvent>().take(1).collect {
                job.cancel()
            }

            conn.gamerules.set("send_command_feedback", true)
            conn.serverSettings.allowFlight.set(true)
            conn.server.sendMessage(
                conn.players.get(), // Targets
                MessageDto(
                    literal = conn.banList.get().joinToString(", ") { it.player.name!! }
                ),
            )

            val event = conn.await() // Wait for connection close by peer
            println(event.cause)
        }
}
```

#### Custom Events

```kotlin
@Serializable
public data class PlayerJoinedEvent(val eventCtx: PlayerDto) : MsmpEvent

public data object ServerStartedEvent : MsmpEvent

@Serializable
public data class IPBanRemovedEvent(val eventCtx: String) : MsmpEvent

// Not a top-level declaration
MsmpEventRegistry.configure {
     "minecraft:notification/players/joined" register PlayerJoinedEvent.serializer()

     "minecraft:notification/server/started" bind ServerStartedEvent

     "minecraft:notification/ip_bans/removed".define { p ->
         IPBanRemovedEvent(p?.jsonPrimitive?.content ?: "")
     }
}
```

#### Custom Request Extensions

```kotlin
public class ServerExtension(public val connection: MsmpConnection) {
    internal val baseEndpoint: String = "minecraft:server"

    public suspend inline fun save(flush: Boolean = false): Boolean {
        val result = connection.call("$baseEndpoint/save", mapOf("flush" to flush))
        return result.jsonPrimitive.boolean
    }
    
    // And more...
}

public val MsmpConnection.server: ServerExtension
    by msmpExtension("minecraft:server") { ServerExtension(it) }

// Usage
val conn: MsmpConnection
conn.server.save(true)

// Or you can also parse `registryName` to the following lambda
public val MsmpConnection.allowList: UniversalArrayExtension<PlayerDto>
    by msmpExtension("minecraft:allowlist") { UniversalArrayExtension(it, this) }
```

### Remote Console

`tech.aliorpse.mcutils:mcutils-rcon:$version`

```kotlin
runBlocking {
    val connection = MCServer.createRconConnection("localhost", password = "mcutilsTest")
    connection.use { println(it.execute("help")) }
}
```

### Player Profile

`tech.aliorpse.mcutils:mcutils-player:$version`

> [!tip]
> This module requires a Ktor client engine, e.g., `ktor-client-cio`.

```kotlin
runBlocking {
    var pl
    pl = MCPlayer.getUuid("Aliorpse") // "ec042e1200ac4a249cc83eb1fab0bd88"
    pl = MCPlayer.getProfile("Aliorpse")
    pl = MCPlayer.getProfile("ec042e1200ac4a249cc83eb1fab0bd88")
    pl = MCPlayer.getProfile("ec042e12-00ac-4a24-9cc8-3eb1fab0bd88")
}
```

Check out the project's [dokka](https://aliorpse.github.io/mcutils/) for the full API reference. There may be some extension functions you’ll find useful.
