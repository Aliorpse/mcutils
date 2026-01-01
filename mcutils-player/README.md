# Player Profile

`tech.aliorpse.mcutils:mcutils-player:$version`

> [!tip]
> This module requires a Ktor client engine (e.g., `ktor-client-cio`).

## Common Usage

### Get UUID

```kotlin
val uuid = MCPlayer.getUuid("Aliorpse") // ec042e1200ac4a249cc83eb1fab0bd88
```

### Get Profile

```kotlin
// Retrieve a player's profile by name or UUID (dashed or undashed)
val profile = MCPlayer.getProfile("Aliorpse")

println(profile.id)
println(profile.name)
println(profile.skinUrl)
```

For the full API reference, please refer to the project's [dokka](https://aliorpse.github.io/mcutils/player/).
