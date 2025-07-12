package tech.aliorpse.mcutils.status

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class ServerPingTest {
    @Test
    fun javaGetStatusTest() = runBlocking {
        val result = JavaPing.getStatus("wdsj.net")
        println(result)

        assert(result.ping > 0)
    }

    @Test
    fun bedrockGetStatusTest() = runBlocking {
        val result = BedrockPing.getStatus("play.easecation.net")
        println(result)

        assert(result.ping > 0)
    }
}
