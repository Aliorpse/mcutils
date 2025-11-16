package tech.aliorpse.mcutils

import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.api.MCServer
import kotlin.test.Test

class ServerStatusTest {
    @Test
    fun `fetch java server status`() {
        val result = runBlocking {
            MCServer.getJavaStatus("demo.mcstatus.io")
        }

        assert(result.ping > 0)
        println(result)
    }

    @Test
    fun `fetch bedrock server status`() {
        val result = runBlocking {
            MCServer.getBedrockStatus("play.easecation.net")
        }

        assert(result.ping > 0)
        println(result)
    }
}
