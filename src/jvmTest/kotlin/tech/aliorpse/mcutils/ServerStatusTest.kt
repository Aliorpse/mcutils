package tech.aliorpse.mcutils

import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.api.MCServer
import kotlin.test.Test

class ServerStatusTest {
    @Test
    fun `fetch java server status`() = runBlocking {
        val result = MCServer.getJavaStatus("demo.mcstatus.io")

        assert(result.ping > 0)
        println(result)
    }

    @Test
    fun `fetch bedrock server status`() = runBlocking {
        val result = MCServer.getBedrockStatus("play.easecation.net")

        assert(result.ping > 0)
        println(result)
    }
}
