plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.20"

    id("love.forte.plugin.suspend-transform") version "2.2.20-0.13.2"
    id("com.google.devtools.ksp") version "2.2.20-2.0.4"
    id("org.jetbrains.dokka") version "2.1.0"
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "tech.aliorpse"
version = System.getenv("GITHUB_REF_NAME") ?: "local"

kotlin {
    jvmToolchain(21)

    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }

    explicitApi()
}

suspendTransformPlugin {
    transformers {
        useJvmDefault()
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    api("io.ktor:ktor-client-core:3.3.1")
    api("io.ktor:ktor-client-content-negotiation:3.3.1")
    api("io.ktor:ktor-serialization-kotlinx-json:3.3.1")
    testImplementation("io.ktor:ktor-client-cio:3.3.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
    publishToMavenCentral()
    signAllPublications()
    coordinates("tech.aliorpse", "mcutils", version.toString())

    pom {
        name = "mcutils"
        description = "A Kotlin-based library that provides utility functions for Minecraft-related queries."
        url = "https://github.com/Aliorpse/mcutils/"
        inceptionYear = "2025"

        licenses {
            license {
                name = "MIT"
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
