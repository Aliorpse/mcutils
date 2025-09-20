package tech.aliorpse.mcutils.server

import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.modules.server.BedrockServer
import tech.aliorpse.mcutils.modules.server.JavaServer
import tech.aliorpse.mcutils.utils.hostPortOf
import kotlin.test.Test

class ServerStatusTest {
    @Test
    fun javaGetStatusTest() {
        val result = runBlocking { JavaServer.getStatus(hostPortOf("wdsj.net")) }

        println(result)
        assert(result.version.protocol > 0)
    }

    @Test
    fun bedrockGetStatusTest() {
        val result = runBlocking { BedrockServer.getStatus(hostPortOf("asia.easecation.net:19132")) }

        println(result)
        assert(result.ping > 0)
    }
}
