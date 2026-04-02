plugins {
    id("org.jetbrains.dokka")
}

group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").orNull ?: "dev"
