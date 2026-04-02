@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    id("mcutils.library-nobrowser")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.shared)
            implementation(projects.util)
            api(projects.color)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.network)
            implementation(libs.kotlinx.io.core)
        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
