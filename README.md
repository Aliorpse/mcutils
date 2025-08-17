# mcutils

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/d83f9fcc077b448f9ce2a40865b17343)](https://app.codacy.com/gh/Aliorpse/kotlin-mcutils/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![CodeFactor](https://www.codefactor.io/repository/github/aliorpse/kotlin-mcutils/badge/main)](https://www.codefactor.io/repository/github/aliorpse/kotlin-mcutils/overview/main)
[![Maven Central](https://maven-badges.sml.io/sonatype-central/tech.aliorpse/mcutils/badge.svg)](https://central.sonatype.com/artifact/tech.aliorpse/mcutils)

A Kotlin-based Minecraft request library provides utility functions related to Minecraft queries.

## Installation

```kotlin
dependencies {
    implementation("tech.aliorpse:mcutils:$version")
}
```

## Features

### Server Status

#### [Demo API Endpoint](https://api.aliorpse.tech/minecraft/server/status/java/hypixel.net)

```kotlin
runBlocking {
    var status
    status = JavaServer.getStatus("mc.hypixel.net")
    
    // More options
    status = JavaServer.getStatus(
        host = "wdsj.net",
        port = 25565,
        enableSrv = true
    )
    
    // Bedrock servers
    status = BedrockServer.getStatus("play.easecation.net")

    println(status)
}
```

### Player Profile (Java Edition Only)

#### [Demo API Endpoint](https://api.aliorpse.tech/minecraft/player/profile/name/Aliorpse)

```kotlin
runBlocking {
    var pl
    pl = Player.getProfile("Aliorpse", Player.IDType.NAME)
    pl = Player.getProfile("ec042e1200ac4a249cc83eb1fab0bd88", Player.IDType.UUID)

    println(pl)
}
```

## Java Usage

All asynchronous methods also provide a blocking variant,  
allowing you to call them directly from Java without dealing with coroutines.

```java
JavaServerStatus status = JavaServer.getStatusBlocking("mc.hypixel.net");
```
