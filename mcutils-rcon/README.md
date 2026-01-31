# Remote Console (RCON)

`tech.aliorpse.mcutils:mcutils-rcon`

## Common Usage

### Execution

```kotlin
val connection = MCServer.createRconConnection("localhost", password = "your_password")
connection.use { rcon ->
    val response = rcon.execute("help")
    println(response)
}
```

For the full API reference, please refer to the project's [dokka](https://aliorpse.github.io/mcutils/rcon/).
