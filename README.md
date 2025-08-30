# mcutils

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Aliorpse_kotlin-mcutils&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Aliorpse_kotlin-mcutils)
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

### Get Server Status

#### [Demo API Endpoint](https://api.aliorpse.tech/minecraft/server/status/hypixel.net:25565?type=java)

```kotlin
runBlocking {
    var status
    status = JavaServer.getStatus("mc.hypixel.net")
    
    // more options
    status = JavaServer.getStatus(
        host = "wdsj.net",
        port = 25565,
        enableSrv = true
    )
    
    // bedrock servers
    status = BedrockServer.getStatus("play.easecation.net")
}
```

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

### [Modrinth](https://modrinth.com/) API

```kotlin
runBlocking {
    var result
    result = Modrinth.search("xplus") {
        author = "Wudji"
        type = ProjectType.MODPACK
    }
    
    result = Modrinth.getProject("2H1rLgy4")
    
    result = Modrinth.getProjects(listOf("2H1rLgy4"))
    
    result = Modrinth.getProjectsRandom(5)
}
```

## Java Usage

All suspend functions also provide a `CompletableFuture` variant,  
allowing you to call them directly from Java without dealing with coroutines.

```java
CompletableFuture<JavaServerStatus> status = JavaServer.getStatusAsync("mc.hypixel.net");
```
