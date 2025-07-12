plugins {
    kotlin("jvm") version "2.1.21"
    id("org.jetbrains.dokka") version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.33.0"
}

repositories {
    mavenCentral()
}

tasks.dokkaJavadoc {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaJavadoc)
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
}

configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
    publishToMavenCentral()
    signAllPublications()
    coordinates("tech.aliorpse", "mcutils", "0.1.1")

    pom {
        name.set("mcutils")
        description.set("Kotlin library for minecraft operations")
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
