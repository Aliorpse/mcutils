# mcutils

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Aliorpse_mcutils&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Aliorpse_mcutils)
[![Maven Central](https://maven-badges.sml.io/sonatype-central/tech.aliorpse/mcutils/badge.svg)](https://central.sonatype.com/artifact/tech.aliorpse/mcutils)
[![View on DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Aliorpse/mcutils)

A Kotlin multiplatform library provides utility functions for Minecraft-related queries.

## Installation

```kotlin
implementation("tech.aliorpse:mcutils:$version")
```
> [!tip]
> Make sure to include a Ktor client engine, such as `ktor-client-cio`.

## Features

### Get Server Status

```kotlin
runBlocking {
    var status
    status = MCServer.getStatus("mc.hypixel.net")
    
    status = MCServer.getStatus(
        host = "wdsj.net",
        port = 25565,
        enableSrv = false
    )
}
```

To reduce package size and avoid using libraries that may cause issues, in some regions (e.g. China), default SRV resolve implementation may be unavailable.

### Get Player Profile

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

## Java Usage

The project uses [kotlin-suspend-transform-compiler-plugin](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin) to automatically generate variants.

```java
CompletableFuture<ServerStatus> status =
        MinecraftServer.getStatusAsync("mc.hypixel.net");
```
