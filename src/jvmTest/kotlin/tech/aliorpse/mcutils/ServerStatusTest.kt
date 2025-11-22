package tech.aliorpse.mcutils

import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.api.MCServer
import kotlin.test.Test

class ServerStatusTest {
    @Test
    fun `fetch java server status`() = runBlocking {
        val result = MCServer.getStatus("demo.mcstatus.io")

        assert(result.ping > 0)
        println(result)
    }
}
