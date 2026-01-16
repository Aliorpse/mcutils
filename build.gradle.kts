@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.abi.AbiValidationMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.dokka)
}

fun Project.hasKtorNetworkDependency(): Boolean =
    configurations.any { it.dependencies.any { deps -> deps.name.contains("ktor-network") } }

val jvmTarget = 17

val isFullBuild = project.findProperty("full-build") == "true"

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
            publishToMavenCentral(automaticRelease = true, validateDeployment = false)
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
            jvmToolchain(jvmTarget)
            explicitApi()
            applyDefaultHierarchyTemplate()

            @OptIn(ExperimentalAbiValidation::class)
            (this as ExtensionAware).extensions.configure<AbiValidationMultiplatformExtension>("abiValidation") {
                enabled.set(true)
            }

            jvm {
                compilations.configureEach {
                    compileTaskProvider.configure {
                        compilerOptions {
                            freeCompilerArgs.addAll("-jvm-default=enable", "-Xjsr305=strict")
                        }
                    }
                }
            }

            afterEvaluate {
                js(IR) {
                    nodejs()
                    if (!hasKtorNetworkDependency()) browser()
                }

                wasmJs {
                    nodejs()
                    if (!hasKtorNetworkDependency()) browser()
                }
            }

            linuxArm64()
            linuxX64()

            if (isFullBuild) { // My poor computer...
                androidNativeX64()
                androidNativeArm64()
                iosArm64()
                iosX64()
                iosSimulatorArm64()
                macosArm64()
                macosX64()
                mingwX64()
            }
        }
    }
}

dependencies {
    dokka(project(":color"))
    dokka(project(":shared"))
    dokka(project(":player"))
    dokka(project(":rcon"))
    dokka(project(":server-status"))
    dokka(project(":msmp"))
}
