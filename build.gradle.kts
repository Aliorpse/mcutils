plugins {
    id("mcutils.root")
}

dependencies {
    dokka(projects.color)
    dokka(projects.shared)
    dokka(projects.player)
    dokka(projects.rcon)
    dokka(projects.serverStatus)
    dokka(projects.msmp)
}
