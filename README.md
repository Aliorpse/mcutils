# mcutils

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Aliorpse_mcutils&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Aliorpse_mcutils)
[![CodeFactor](https://www.codefactor.io/repository/github/aliorpse/mcutils/badge/main)](https://www.codefactor.io/repository/github/aliorpse/mcutils/overview/main)
[![Maven Central](https://maven-badges.sml.io/sonatype-central/tech.aliorpse/mcutils/badge.svg)](https://central.sonatype.com/artifact/tech.aliorpse/mcutils)
[![View on DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Aliorpse/mcutils)

A Kotlin-based library that provides utility functions for Minecraft-related queries.

This project has not been widely tested, so use it at your own risk in production environments. Contributions to improve it are welcome.

## Installation

```kotlin
dependencies {
    implementation("tech.aliorpse:mcutils:$version")
    // Add the HTTP client you prefer
    implementation("io.ktor:ktor-client-cio:$version")
}
```

You can use `McUtilsConfig.httpClient.use()` to explicitly control what client to be used.

## Features

### Get Server Status

#### [Demo API Endpoint](https://api.aliorpse.tech/minecraft/server/status/hypixel.net:25565?type=java)

```kotlin
runBlocking {
    var status
    status = JavaServer.getStatus("mc.hypixel.net")
    
    status = JavaServer.getStatus(
        host = "wdsj.net",
        port = 25565,
        enableSrv = true
    )
    
    status = BedrockServer.getStatus("play.easecation.net")
}
```

In some regions (e.g. China), default DNS lookup implementation may be unavailable. You can change `McUtilsConfig.dns.srvResolver`.

### Get Player Profile (Java Edition Only)

#### [Demo API Endpoint](https://api.aliorpse.tech/minecraft/player/profile/Aliorpse)

```kotlin
runBlocking {
    var pl
    pl = Player.getProfile("Aliorpse")
    pl = Player.getProfile("ec042e1200ac4a249cc83eb1fab0bd88")
    pl = Player.getProfile("ec042e12-00ac-4a24-9cc8-3eb1fab0bd88")
}
```

Check out the project's [dokka](https://aliorpse.github.io/mcutils/) for the full API reference. There may be some extension functions you’ll find useful.

## Java Usage

The project uses [kotlin-suspend-transform-compiler-plugin](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin) to generate variants automatically.

For every suspending API, both **async** and **blocking** variants are available.

```java
CompletableFuture<JavaServerStatus> status =
        JavaServer.getStatusAsync("mc.hypixel.net");
```
