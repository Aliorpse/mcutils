package tech.aliorpse.mcutils

import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.annotation.ExperimentalMCUtilsApi
import tech.aliorpse.mcutils.api.MCServer
import kotlin.test.Test

class ServerTest {
    @Test
    fun `fetch java server status by slp`() = runBlocking {
        val result = MCServer.getStatus("hypixel.net")

        assert(result.ping > 0)
        println(result)
    }

    @OptIn(ExperimentalMCUtilsApi::class)
    @Test
    fun `fetch java server status by query`() = runBlocking {
        val result = MCServer.getQueryFull("localhost")
        val result2 = MCServer.getQueryBasic("localhost")
        println(result)
        println(result2)
    }

    @OptIn(ExperimentalMCUtilsApi::class)
    @Test
    fun `test RCON`() = runBlocking {
        val connection = MCServer.createRconConnection("localhost", password = "mcutilsTest")
        connection.use {
            println(it.execute("help"))
        }
    }
}
