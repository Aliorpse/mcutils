package tech.aliorpse.mcutils.server

import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.modules.server.BedrockServer
import tech.aliorpse.mcutils.modules.server.JavaServer
import tech.aliorpse.mcutils.utils.toHtml
import kotlin.test.Test

class ServerStatusTest {
    @Test
    fun javaGetStatusTest() {
        val result = runBlocking { JavaServer.getStatus("hypixel.net") }

        println(result.description.toHtml())
        assert(result.version.protocol > 0)
    }

    @Test
    fun bedrockGetStatusTest() {
        val result = runBlocking { BedrockServer.getStatus("asia.easecation.net") }

        println(result)
        assert(result.ping > 0)
    }
}
