import org.jetbrains.dokka.gradle.tasks.DokkaGeneratePublicationTask

plugins {
    kotlin("jvm") version "2.1.21"
    id("org.jetbrains.dokka") version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.33.0"
}

group = "tech.aliorpse.mcutils"
version = System.getenv("GITHUB_REF_NAME")

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    api("com.google.code.gson:gson:2.13.1")
    api("com.squareup.retrofit2:retrofit:3.0.0")
    api("com.squareup.retrofit2:converter-gson:3.0.0")

    testImplementation(kotlin("test"))
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.test {
    useJUnitPlatform()
}

val dokkaGeneratePublicationHtml by
    tasks.existing(DokkaGeneratePublicationTask::class)

// dokka as javadoc
tasks.withType<GenerateModuleMetadata>().configureEach {
    dependsOn(dokkaGeneratePublicationHtml)
}

tasks.named<Jar>("javadocJar") {
    dependsOn(dokkaGeneratePublicationHtml)
    from(dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
}

// github pages
tasks.named<DokkaGeneratePublicationTask>("dokkaGeneratePublicationHtml") {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
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
