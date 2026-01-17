# Changelog

## [3.6.7](https://github.com/Aliorpse/mcutils/compare/v3.6.6...v3.6.7) (2026-01-17)


### Bug Fixes

* **serialization:** add @Serializable for some public data classes, for convenience ([a8f3514](https://github.com/Aliorpse/mcutils/commit/a8f35143f8b6c5a0b7942d3b5547c800e002c311))

## [3.6.6](https://github.com/Aliorpse/mcutils/compare/v3.6.5...v3.6.6) (2026-01-16)


### Bug Fixes

* **validation:** add -Pfull-build=true for it ([9a4a542](https://github.com/Aliorpse/mcutils/commit/9a4a5420e7a23a699003fb05230bb2f14d864f68))

## [3.6.5](https://github.com/Aliorpse/mcutils/compare/v3.6.4...v3.6.5) (2026-01-16)


### Bug Fixes

* **publish:** no more validateDeployment ([b772c20](https://github.com/Aliorpse/mcutils/commit/b772c2028ab98fbd75cec7be1940b4934269ca12))

## [3.6.4](https://github.com/Aliorpse/mcutils/compare/v3.6.3...v3.6.4) (2026-01-16)


### Performance Improvements

* **msmp:** use KotlinxWebsocketSerializationConverter for automatically serde-ing ([287d85a](https://github.com/Aliorpse/mcutils/commit/287d85af34e88965b9cb0927467f00f253d81133))

## [3.6.3](https://github.com/Aliorpse/mcutils/compare/v3.6.2...v3.6.3) (2026-01-11)


### Bug Fixes

* **publish:** publish mcutils-util ([7086f33](https://github.com/Aliorpse/mcutils/commit/7086f331031841d0bb2a90a907b331025ca18e6d))

## [3.6.2](https://github.com/Aliorpse/mcutils/compare/v3.6.1...v3.6.2) (2026-01-11)


### Bug Fixes

* **publish:** make it publish ([c7a67a0](https://github.com/Aliorpse/mcutils/commit/c7a67a0f501ec1c2a99373603362f1429f21f0fd))

## [3.6.1](https://github.com/Aliorpse/mcutils/compare/v3.6.0...v3.6.1) (2026-01-02)


### Bug Fixes

* **msmp:** remove duplicated code, use get() to replace snapshot() ([f1f18c2](https://github.com/Aliorpse/mcutils/commit/f1f18c22aa5492e3df1815afa74c96a7b3b7eb2e))

## [3.6.0](https://github.com/Aliorpse/mcutils/compare/v3.5.0...v3.6.0) (2026-01-02)


### Features

* MsmpClient, auto-reconnect, request batching, syncable extensions, stabilize several API. ([9d1b2e2](https://github.com/Aliorpse/mcutils/commit/9d1b2e21548be934c70a58e60c8e823e76770074))

## [3.5.0](https://github.com/Aliorpse/mcutils/compare/v3.4.1...v3.5.0) (2026-01-01)


### Features

* **msmp:** simplify extension registration ([35b9dc4](https://github.com/Aliorpse/mcutils/commit/35b9dc4a9e6e9b393d80f3d938936934fa15a164))

## [3.4.1](https://github.com/Aliorpse/mcutils/compare/v3.4.0...v3.4.1) (2025-12-29)


### Performance Improvements

* **msmp:** remove unused annotation ([df133ab](https://github.com/Aliorpse/mcutils/commit/df133ab028574d81d41190caf5c47a9571b4ad64))

## [3.4.0](https://github.com/Aliorpse/mcutils/compare/v3.3.0...v3.4.0) (2025-12-27)


### Features

* **msmp:** support custom event & request API extension registration ([1a74e73](https://github.com/Aliorpse/mcutils/commit/1a74e732bc4d5d861f05c300ffa734b841fd1e29))

## [3.3.0](https://github.com/Aliorpse/mcutils/compare/v3.2.1...v3.3.0) (2025-12-27)


### Features

* **msmp:** add connection close handling, utility functions and code refactor ([b8b7112](https://github.com/Aliorpse/mcutils/commit/b8b71129824adf7f8a18c089b20012afca404c66))

## [3.2.1](https://github.com/Aliorpse/mcutils/compare/v3.2.0...v3.2.1) (2025-12-26)


### Bug Fixes

* **build:** add serialization dependency for server-status module [skip ci] ([6b7b7c2](https://github.com/Aliorpse/mcutils/commit/6b7b7c2ec7ad9705f2400cdd637939da24880f1c))

## [3.2.0](https://github.com/Aliorpse/mcutils/compare/v3.1.1...v3.2.0) (2025-12-26)


### Features

* **msmp:** complete all existing MSMP API wrapping ([9a93104](https://github.com/Aliorpse/mcutils/commit/9a93104dc6983e8683f2712beaca321763ee380d))
* **msmp:** implement basic MSMP API ([eeec9ba](https://github.com/Aliorpse/mcutils/commit/eeec9bae4bb5ecab29af414300e9fde693f8a497))
* **msmp:** implement MSMP event listening ([f6a10b1](https://github.com/Aliorpse/mcutils/commit/f6a10b16f26a158768ed1c09147adada68ba7c94))

## [3.1.1](https://github.com/Aliorpse/mcutils/compare/v3.1.0...v3.1.1) (2025-12-20)


### Bug Fixes

* fix issues migrating to multi-module project ([605d8ab](https://github.com/Aliorpse/mcutils/commit/605d8ab3fb6ddbf5152f46a32bc59334d0edbc70))

## [3.1.0](https://github.com/Aliorpse/mcutils/compare/v3.0.1...v3.1.0) (2025-12-15)


### Features

* add iosSimulatorArm64 ([74eb2bf](https://github.com/Aliorpse/mcutils/commit/74eb2bf31ff2698b0fbfe23ab77866f5b86fd31f))

## [3.0.1](https://github.com/Aliorpse/mcutils/compare/v3.0.0...v3.0.1) (2025-12-14)


### Bug Fixes

* publish and docs ([947107b](https://github.com/Aliorpse/mcutils/commit/947107bb293d2aab20df2febc726b00226b4137a))

## [3.0.0](https://github.com/Aliorpse/mcutils/compare/v2.5.2...v3.0.0) (2025-12-14)


### ⚠ BREAKING CHANGES

* make the library multi-module

### Code Refactoring

* make the library multi-module ([8d70866](https://github.com/Aliorpse/mcutils/commit/8d70866debfbf400e9af4ea276ebf4818a14ce54))

## [2.5.2](https://github.com/Aliorpse/mcutils/compare/v2.5.1...v2.5.2) (2025-12-07)


### Bug Fixes

* **build:** no more browser target ([7a87ea6](https://github.com/Aliorpse/mcutils/commit/7a87ea696ce8f16475b3e2b66452cc52a66639e4))

## [2.5.1](https://github.com/Aliorpse/mcutils/compare/v2.5.0...v2.5.1) (2025-12-06)


### Performance Improvements

* improve implementation of `QueryImpl.kt` ([05b2844](https://github.com/Aliorpse/mcutils/commit/05b2844f0b725f072010c1fa4c99e4409bf5acf7))

## [2.5.0](https://github.com/Aliorpse/mcutils/compare/v2.4.1...v2.5.0) (2025-11-30)


### Features

* **query:** implement Query protocol ([897b0b8](https://github.com/Aliorpse/mcutils/commit/897b0b8ca8d494fd2aef8fb4195695e3d9e3297d))

## [2.4.1](https://github.com/Aliorpse/mcutils/compare/v2.4.0...v2.4.1) (2025-11-29)


### Performance Improvements

* implement DispatchersIO for different platforms ([9f1a6d7](https://github.com/Aliorpse/mcutils/commit/9f1a6d70e996592c677fe529150894582eef9e8e))

## [2.4.0](https://github.com/Aliorpse/mcutils/compare/v2.3.0...v2.4.0) (2025-11-29)


### Features

* **rcon:** support RCON protocol ([ab061ad](https://github.com/Aliorpse/mcutils/commit/ab061ad36a1fa38d01d1a8b06e0aefcc948f7d4c))

## [2.3.0](https://github.com/Aliorpse/mcutils/compare/v2.2.1...v2.3.0) (2025-11-28)


### Features

* **annotation:** add `InternalMCUtilsApi` and add some descriptions to `ExperimentalMCUtilsApi` ([f14e6bb](https://github.com/Aliorpse/mcutils/commit/f14e6bbf806fd5b5783335db97d575e95c8919e4))

## [2.2.1](https://github.com/Aliorpse/mcutils/compare/v2.2.0...v2.2.1) (2025-11-28)


### Bug Fixes

* **js/security:** bump glob to 10.5.0 ([e07d41d](https://github.com/Aliorpse/mcutils/commit/e07d41d01ba7ad42512e8ac35275d2f0cb074b87))
* **variant:** add variants for `MinecraftProtocol` ([6418498](https://github.com/Aliorpse/mcutils/commit/641849885dfc50335b23c42ac4188a0196ec4822))

## [2.2.0](https://github.com/Aliorpse/mcutils/compare/v2.1.3...v2.2.0) (2025-11-23)


### Features

* **protocol:** extract Minecraft protocol functions to `MinecraftProtocol` and make them public ([da65d78](https://github.com/Aliorpse/mcutils/commit/da65d78288193fa50d26a2a0644751c2a544415d))
* **protocol:** extract Minecraft protocol functions to `MinecraftProtocol` and make them public ([2af4d7d](https://github.com/Aliorpse/mcutils/commit/2af4d7d3ed28b51f0b4a95ed18e6e6c9b6b6fbcb))

## [2.1.3](https://github.com/Aliorpse/mcutils/compare/v2.1.2...v2.1.3) (2025-11-23)


### Bug Fixes

* **ci:** use -Pversion to pass the version ([a0b2209](https://github.com/Aliorpse/mcutils/commit/a0b2209e2e5c5452b765143e3d6d0412f1aff166))
* **dependencies:** remove unused dependencies ([bd8c6d3](https://github.com/Aliorpse/mcutils/commit/bd8c6d3b8fcd47c62fa2e3d8905f62dec971bb01))
* **test:** a test commit to test ci ([3824737](https://github.com/Aliorpse/mcutils/commit/38247372a46fe71fbe4ad290908fb985e92aae9b))
