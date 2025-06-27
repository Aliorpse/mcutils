plugins {
    kotlin("jvm") version "2.1.21"
    `maven-publish`
}

group = "tech.aliorpse"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.code.gson:gson:2.13.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
