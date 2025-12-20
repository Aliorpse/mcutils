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
