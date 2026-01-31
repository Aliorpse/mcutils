# Server Status

`tech.aliorpse.mcutils:mcutils-server-status`

> [!warning]
> Native targets do not yet support an SRV record implementation. Setting `enableSrv = true` will not have any effect on these platforms.

## Common Usage

### Server List Ping

Fetch basic server information such as MOTD, player counts, and version using the standard SLP protocol:

```kotlin
val status = MCServer.getStatus("mc.hypixel.net")

println(status.description)
println("${status.players.online}/${status.players.max}")
```

### Query

Fetch detailed server information. This requires `enable-query=true` in the server's `server.properties`.

#### Basic Query

```kotlin
val basic = MCServer.getQueryBasic("localhost")
println(basic.motd)
```

#### Full Query

```kotlin
val full = MCServer.getQueryFull("localhost")

println(full.plugins)
println(full.map)
println(full.playerList)
```

For the full API reference, please refer to the project's [dokka](https://aliorpse.github.io/mcutils/server-status/).
