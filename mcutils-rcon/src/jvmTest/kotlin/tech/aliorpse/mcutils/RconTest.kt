package tech.aliorpse.mcutils

import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.api.MCServer
import tech.aliorpse.mcutils.api.createRconConnection
import kotlin.test.Test

class RconTest {
    @Test
    fun `test RCON`() = runBlocking {
        val connection = MCServer.createRconConnection("localhost", password = "mcutilsTest")
        connection.use {
            println(it.execute("help"))
        }
    }
}
