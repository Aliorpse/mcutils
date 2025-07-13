# mcutils

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/d83f9fcc077b448f9ce2a40865b17343)](https://app.codacy.com/gh/Aliorpse/kotlin-mcutils/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)

A Kotlin-based Minecraft request library provides utility functions related to Minecraft queries.

## Installation

### Gradle

```kotlin
dependencies {
    implementation("tech.aliorpse:mcutils:<version>")
}
```

### Maven

```xml
<dependency>
    <groupId>tech.aliorpse</groupId>
    <artifactId>mcutils</artifactId>
    <version><!-- version --></version>
</dependency>
```

## Examples

```kotlin
fun main() = runBlocking {
    var status
    status = JavaPing.getStatus("mc.hypixel.net")
    status = JavaPing.getStatus(
        host = "wdsj.net",
        port = 25565,
        enableSrv = true
    )
    status = BedrockPing.getStatus("play.easecation.net")
    
    var playerInfo
    playerInfo = MojangAPI.getProfile("ec042e1200ac4a249cc83eb1fab0bd88")
    playerInfo = MojangAPI.getProfileByName("Aliorpse")
}
```
