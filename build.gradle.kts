plugins {
    kotlin("jvm") version "2.1.21"
    id("com.vanniktech.maven.publish") version "0.33.0"
    id("org.jetbrains.dokka") version "2.0.0"
}

group = "tech.aliorpse.mcutils"
version = "0.1.1"

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

tasks.register<Jar>("javadocJar") {
    dependsOn("dokkaHtml")
    archiveClassifier.set("javadoc")
    from(layout.buildDirectory.dir("dokka"))
}

kotlin {
    jvmToolchain(21)
}

java {
    withSourcesJar()
}

configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
    publishToMavenCentral()
    signAllPublications()
    coordinates("tech.aliorpse", "mcutils", "0.1.1")

    pom {
        name = "mcutils"
        description = "Kotlin library for minecraft operations"
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