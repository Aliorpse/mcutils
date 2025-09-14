plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"

    id("love.forte.plugin.suspend-transform") version "2.2.0-0.13.1"
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("org.jetbrains.dokka") version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.33.0"
}

group = "tech.aliorpse.mcutils"
version = System.getenv("GITHUB_REF_NAME") ?: "local"

kotlin {
    jvmToolchain(21)

    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }

    explicitApi()
}

dokka {
    dokkaSourceSets {
        named("main") {
            sourceRoots.setFrom(file("src/main/kotlin"))
        }
    }
}

suspendTransformPlugin {
    transformers {
        addJvmBlocking()
        addJvmAsync()
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // kotlinx
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // ktor
    api("io.ktor:ktor-client-core:3.3.0")
    api("io.ktor:ktor-client-content-negotiation:3.3.0")
    api("io.ktor:ktor-serialization-kotlinx-json:3.3.0")
    testImplementation("io.ktor:ktor-client-cio:3.3.0")

    // DNSJava
    api("dnsjava:dnsjava:3.6.3")

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
        description = "Kotlin library for Minecraft operations"
        url = "https://github.com/Aliorpse/kotlin-mcutils/"
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
            url = "https://github.com/Aliorpse/kotlin-mcutils/"
            connection = "scm:git:git://github.com/Aliorpse/kotlin-mcutils.git"
            developerConnection = "scm:git:ssh://git@github.com/Aliorpse/kotlin-mcutils.git"
        }
    }
}
