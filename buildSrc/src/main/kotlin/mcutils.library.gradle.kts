import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("mcutils.library-nobrowser")
}

kotlin {
    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
}
