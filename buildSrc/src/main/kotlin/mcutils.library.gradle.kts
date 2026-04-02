@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    id("mcutils.library-nobrowser")
}

kotlin {
    js(IR) {
        browser()
    }

    wasmJs {
        browser()
    }
}
