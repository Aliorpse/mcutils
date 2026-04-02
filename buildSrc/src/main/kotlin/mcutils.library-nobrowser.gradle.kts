@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import com.vanniktech.maven.publish.DeploymentValidation
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.dsl.abi.AbiValidationMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").orNull ?: "dev"

extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
    publishToMavenCentral(automaticRelease = true, validateDeployment = DeploymentValidation.NONE)
    signAllPublications()

    coordinates(
        project.group.toString(),
        "${rootProject.name}-${project.name}",
        project.version.toString()
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

kotlin {
    jvmToolchain(17)
    explicitApi()
    applyDefaultHierarchyTemplate()

    @OptIn(ExperimentalAbiValidation::class)
    (this as ExtensionAware).extensions.configure<AbiValidationMultiplatformExtension>("abiValidation") {
        enabled = true
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

    linuxArm64()
    linuxX64()
    androidNativeX64()
    androidNativeArm64()
    iosArm64()
    iosX64()
    iosSimulatorArm64()
    macosArm64()
    mingwX64()

    js(IR) {
        nodejs()
    }

    wasmJs {
        nodejs()
    }
}
