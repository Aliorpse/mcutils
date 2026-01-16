plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":shared"))
            implementation(project(":util"))
            implementation(libs.ktor.network)
        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
