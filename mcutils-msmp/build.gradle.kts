plugins {
    id("mcutils.library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.shared)
            implementation(projects.util)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        jvmTest.dependencies {
            implementation(libs.ktor.client.cio)
            implementation(kotlin("test"))
        }
    }
}
