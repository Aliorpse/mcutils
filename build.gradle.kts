plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.dokka)
}

allprojects {
    group = project.property("group") as String
    version = project.property("version") as String

    repositories {
        mavenCentral()
    }
}

subprojects {
    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            publishToMavenCentral()
            signAllPublications()

            coordinates(
                group.toString(),
                "${rootProject.name}-${project.name}",
                version.toString()
            )

            pom {
                name = "${rootProject.name}-${project.name}"
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
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
            jvmToolchain(21)
            explicitApi()
            applyDefaultHierarchyTemplate()

            iosArm64()
            iosX64()
            iosSimulatorArm64()
            linuxArm64()
            linuxX64()
            macosArm64()
            macosX64()
            mingwX64()

            js(IR) { nodejs() }

            jvm {
                compilations.configureEach {
                    compileTaskProvider.configure {
                        compilerOptions {
                            freeCompilerArgs.add("-jvm-default=enable")
                        }
                    }
                }
            }

            targets.all {
                compilations.all {
                    compileTaskProvider.configure {
                        compilerOptions {
                            freeCompilerArgs.add("-Xexplicit-backing-fields")
                        }
                    }
                }
            }
        }
    }
}

dependencies {
    dokka(project(":color"))
    dokka(project(":core"))
    dokka(project(":player"))
    dokka(project(":rcon"))
    dokka(project(":server-status"))
    dokka(project(":msmp"))
}
