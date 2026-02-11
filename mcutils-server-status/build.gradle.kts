plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":shared"))
            implementation(project(":util"))
            api(project(":color"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.network)
            implementation(libs.kotlinx.io.core)
        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
