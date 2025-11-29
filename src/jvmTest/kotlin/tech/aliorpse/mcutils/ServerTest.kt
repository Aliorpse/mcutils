package tech.aliorpse.mcutils

import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.annotation.ExperimentalMCUtilsApi
import tech.aliorpse.mcutils.api.MCServer
import kotlin.test.Test

class ServerTest {
    @Test
    fun `fetch java server status`() = runBlocking {
        val result = MCServer.getStatus("wdsj.net")

        assert(result.ping > 0)
        println(result)
    }

    @OptIn(ExperimentalMCUtilsApi::class)
    fun `test RCON`() = runBlocking {
        val connection = MCServer.createRconConnection("localhost", password = "mcutilsTest")
        connection.use {
            println(it.execute("help"))
        }
    }
}
