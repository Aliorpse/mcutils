package tech.aliorpse.mcutils.modrinth

import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.model.modrinth.ProjectType
import tech.aliorpse.mcutils.modules.modrinth.Modrinth
import kotlin.test.Test

class Test {
    @Test
    fun searchTest() {
        val result = runBlocking {
            Modrinth.search("xplus") {
                author = "Wudji"
                type = ProjectType.MODPACK
            }
        }

        println(result)
    }

    @Test
    fun getProjectsTest() {
        val result = runBlocking {
            Modrinth.getProject("2H1rLgy4")
        }

        println(result)
    }

    @Test
    fun getProjectsRandomTest() {
        val result = runBlocking {
            Modrinth.getProjectsRandom(6)
        }

        println(result)
    }
}
