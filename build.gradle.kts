plugins {
    kotlin("jvm") version "2.2.0"
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
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // kotlinx
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // retrofit
    api("com.squareup.retrofit2:retrofit:3.0.0")
    api("com.squareup.retrofit2:converter-moshi:3.0.0")

    // moshi
    api("com.squareup.moshi:moshi:1.15.2")
    api("com.squareup.moshi:moshi-kotlin:1.15.2")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.2")

    // DNSJava
    api("dnsjava:dnsjava:3.6.3")
    implementation("org.slf4j:slf4j-nop:2.0.17")

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
        name.set("mcutils")
        description.set("Kotlin library for Minecraft operations")
        url.set("https://github.com/Aliorpse/kotlin-mcutils/")
        inceptionYear.set("2025")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("aliorpse")
                name.set("Aliorpse")
                url.set("https://github.com/Aliorpse/")
            }
        }

        scm {
            url.set("https://github.com/Aliorpse/kotlin-mcutils/")
            connection.set("scm:git:git://github.com/Aliorpse/kotlin-mcutils.git")
            developerConnection.set("scm:git:ssh://git@github.com/Aliorpse/kotlin-mcutils.git")
        }
    }
}
