plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "mcutils"

include(":color")
project(":color").projectDir = file("mcutils-color")

include(":core")
project(":core").projectDir = file("mcutils-core")

include(":player")
project(":player").projectDir = file("mcutils-player")

include(":rcon")
project(":rcon").projectDir = file("mcutils-rcon")

include(":server-status")
project(":server-status").projectDir = file("mcutils-server-status")

include(":util")
project(":util").projectDir = file("mcutils-util")

include("msmp")
project(":msmp").projectDir = file("mcutils-msmp")
