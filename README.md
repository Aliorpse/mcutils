# mcutils

[![CodeFactor](https://www.codefactor.io/repository/github/aliorpse/mcutils/badge)](https://www.codefactor.io/repository/github/aliorpse/mcutils)
![Maven Central](https://maven-badges.sml.io/sonatype-central/tech.aliorpse.mcutils/mcutils-shared/badge.svg)
[![View on DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Aliorpse/mcutils)

mcutils is a lightweight **Kotlin Multiplatform** library for Minecraft Java related queries, such as server
ping/management, fetching player profile, etc.

## Supported platforms

* **Kotlin/JVM**
* **Kotlin/JS, WasmJS** on
  * nodejs
  * browser *(except modules that depend on ktor-network, see [build.gradle.kts](build.gradle.kts) for more details)*
* **Kotlin/Native** on
  * linuxX64 (X64/Arm64)
  * mingw (X64)
  * ios (X64/Arm64)
  * iosSimulator (Arm64)
  * macos (X64/Arm64)
  * androidNative (X64/Arm64)

## Modules

- [Server Status](mcutils-server-status/README.md): Fetch Minecraft server status via SLP or Query.
- [Minecraft Server Management Protocol (MSMP)](mcutils-msmp/README.md): Comprehensive server management and event listening.
- [Remote Console (RCON)](mcutils-rcon/README.md): Execute commands remotely on the server.
- [Player Profile](mcutils-player/README.md): Retrieve player UUIDs and profiles.

Click on the links for module-specific documents, or check out the project's [dokka](https://aliorpse.github.io/mcutils/) for the full API reference.


## Contributing

Please refer to [CONTRIBUTING.md](CONTRIBUTING.md) for more details.
