plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.suspend.transform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kotlinx.rpc)
}

group = project.property("group") as String
version = System.getenv("GITHUB_REF_NAME") ?: "local"

kotlin {
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    js(IR) {
        browser()
        nodejs()
    }
    jvm()
    linuxArm64()
    linuxX64()
    macosArm64()
    macosX64()
    mingwX64()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.collections.immutable)

            api(libs.ktor.client.core)
            api(libs.ktor.network)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.serialization.kotlinx.json)

            api(libs.kotlinx.rpc.client)
            api(libs.kotlinx.rpc.serialization.json)
            api(libs.kotlinx.rpc.ktor.client)
        }

        jvmTest.dependencies {
            implementation(libs.ktor.client.cio)
            implementation(kotlin("test"))
        }
    }

    jvmToolchain(21)
    explicitApi()
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

suspendTransformPlugin {
    transformers {
        useDefault()
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), rootProject.name, version.toString())

    pom {
        name = rootProject.name
        description = "A Kotlin multiplatform library provides utility functions for Minecraft-related queries."
        url = "https://github.com/Aliorpse/mcutils/"
        inceptionYear = "2025"

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
            }
        }

        developers {
            developer {
                id = "aliorpse"
                name = "Aliorpse"
                url = "https://github.com/Aliorpse/"
            }
        }

        scm {
            url = "https://github.com/Aliorpse/mcutils/"
            connection = "scm:git:git://github.com/Aliorpse/mcutils.git"
            developerConnection = "scm:git:ssh://git@github.com/Aliorpse/mcutils.git"
        }
    }
}
