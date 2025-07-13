# mcutils

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/d83f9fcc077b448f9ce2a40865b17343)](https://app.codacy.com/gh/Aliorpse/kotlin-mcutils/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)

基于 Kotlin 的 mc 请求库, 封装了与 Minecraft 查询相关的实用功能。

## 安装

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

## 使用示例

项目文档较为详细, 这里不多赘述

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
