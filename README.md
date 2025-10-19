# mcutils

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Aliorpse_mcutils&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Aliorpse_mcutils)
[![CodeFactor](https://www.codefactor.io/repository/github/aliorpse/mcutils/badge/main)](https://www.codefactor.io/repository/github/aliorpse/mcutils/overview/main)
[![Maven Central](https://maven-badges.sml.io/sonatype-central/tech.aliorpse/mcutils/badge.svg)](https://central.sonatype.com/artifact/tech.aliorpse/mcutils)
[![View on DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Aliorpse/mcutils)

A Kotlin-based library that provides utility functions for Minecraft-related queries.

This project hasn't been extensively tested, so use it at your own risk in production environments. Contributions to improve it are welcome.

## Installation

```kotlin
dependencies {
    implementation("tech.aliorpse:mcutils:$version")
    // Add the HTTP client you prefer
    implementation("io.ktor:ktor-client-cio:$version")
}
```

You can use `McUtilsConfig.httpClient.use()` to explicitly specify which client to use.

## Features

### Get Server Status

#### [Example API Endpoint](https://api.aliorpse.tech/minecraft/server/status/hypixel.net:25565?type=java)

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

To reduce package size and avoid using libraries that may cause issues, in some regions (e.g. China), default SRV resolve implementation may be unavailable. Change `McUtilsConfig.dns.srvResolver` to override.

### Get Player Profile (Java Edition Only)

#### [Example API Endpoint](https://api.aliorpse.tech/minecraft/player/profile/Aliorpse)

```kotlin
runBlocking {
    var pl
    pl = Player.getProfile("Aliorpse")
    pl = Player.getProfile("ec042e1200ac4a249cc83eb1fab0bd88")
    pl = Player.getProfile("ec042e12-00ac-4a24-9cc8-3eb1fab0bd88")
}
```

### Server Management Protocol (WIP)

```kotlin
runBlocking {
    val client = MsmpClient("ws://<port>:<port>", "your token here")
    
    client.withConnection {
        allowlist.clear()
        println(allowlist.add("Aliorpse", "MosCro"))
        println(allowlist.set("MosCro", "Technoblade", "Aliorpse"))
        println(allowlist.remove("MosCro", "Technoblade"))
        println(allowlist.get())
    }
    
    val connection = client.connect() // or just use the MsmpClient.connect(host, token)
    connection.allowlist.add("Aliorpse")
    connection.close() // must execute this to release the object and prevent the server from throwing an error
}
```

Check out the project's [dokka](https://aliorpse.github.io/mcutils/) for the full API reference. There may be some extension functions you’ll find useful.

## Java Usage

The project uses [kotlin-suspend-transform-compiler-plugin](https://github.com/ForteScarlet/kotlin-suspend-transform-compiler-plugin) to automatically generate variants.

For every suspending API, both **async** and **blocking** variants are available.

```java
CompletableFuture<JavaServerStatus> status =
        JavaServer.getStatusAsync("mc.hypixel.net");
```
