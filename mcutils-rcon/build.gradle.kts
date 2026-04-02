@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    id("mcutils.library-nobrowser")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.shared)
            implementation(projects.util)
            implementation(libs.ktor.network)
        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
