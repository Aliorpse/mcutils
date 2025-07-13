package tech.aliorpse.mcutils.status

import kotlin.test.Test

class ServerPingTest {
    @Test
    fun javaGetStatusTest() {
        val result = JavaPing.getStatusBlocking("mc.hypixel.net")
        println(result)
        assert(result.ping > 0)
    }

    @Test
    fun bedrockGetStatusTest() {
        val result = BedrockPing.getStatusBlocking("play.easecation.net")
        println(result)
        assert(result.ping > 0)
    }
}
