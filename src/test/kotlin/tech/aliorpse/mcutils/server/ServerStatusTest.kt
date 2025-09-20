package tech.aliorpse.mcutils.server

import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.modules.server.BedrockServer
import tech.aliorpse.mcutils.modules.server.JavaServer
import kotlin.test.Test

class ServerStatusTest {
    @Test
    fun javaGetStatusTest() {
        val result = runBlocking { JavaServer.getStatus("wdsj.net") }

        println(result)
        assert(result.version.protocol > 0)
    }

    @Test
    fun bedrockGetStatusTest() {
        val result = runBlocking { BedrockServer.getStatus("asia.easecation.net") }

        println(result)
        assert(result.ping > 0)
    }
}
