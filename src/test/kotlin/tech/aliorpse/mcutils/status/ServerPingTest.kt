package tech.aliorpse.mcutils.status

import tech.aliorpse.mcutils.module.status.BedrockPing
import tech.aliorpse.mcutils.module.status.JavaPing
import kotlin.test.Test

class ServerPingTest {
    @Test
    fun javaGetStatusTest() {
        val result = JavaPing.getStatusBlocking("bedrock.mineseed.org")

        println(result)
        assert(result.ping > 0)
    }

    @Test
    fun bedrockGetStatusTest() {
        val result = BedrockPing.getStatusBlocking("asia.easecation.net")
        println(result)
        assert(result.ping > 0)
    }
}
