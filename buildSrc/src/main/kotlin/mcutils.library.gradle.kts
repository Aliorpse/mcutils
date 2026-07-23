@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    id("mcutils.library-nobrowser")
}

kotlin {
    js {
        browser()
    }

    wasmJs {
        browser()
    }
}
