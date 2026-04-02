dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "mcutils"

include("color", "shared", "player", "rcon", "server-status", "util", "msmp")

rootProject.children.forEach {
    it.projectDir = file("mcutils-${it.name}")
}
