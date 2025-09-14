package tech.aliorpse.mcutils.modrinth

import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.model.modrinth.ProjectType
import tech.aliorpse.mcutils.modules.modrinth.Modrinth
import tech.aliorpse.mcutils.utils.McUtilsHttpClient
import kotlin.test.Test

class Test {
    @Test
    fun searchTest() {
        McUtilsHttpClient.init(CIO)
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
        McUtilsHttpClient.init(CIO)
        val result = runBlocking {
            Modrinth.getProjects(listOf("2H1rLgy4", "fabric-api"))
        }

        println(result)
    }

    @Test
    fun getProjectsRandomTest() {
        McUtilsHttpClient.init(CIO)
        val result = runBlocking {
            Modrinth.getProjectsRandom(6)
        }

        println(result)
    }
}
