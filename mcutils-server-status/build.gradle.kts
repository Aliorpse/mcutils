plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            implementation(project(":util"))
            api(project(":color"))
            implementation(libs.ktor.network)
                       implementation(libs.kotlinx.serialization.json)
        }
        jvmMain.dependencies {
            implementation(libs.minidns.hla)
        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
