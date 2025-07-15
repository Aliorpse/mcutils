package tech.aliorpse.mcutils.status

import tech.aliorpse.mcutils.module.status.BedrockPing
import tech.aliorpse.mcutils.module.status.JavaPing
import tech.aliorpse.mcutils.util.MOTDParser.objectToSectionFormat
import kotlin.test.Test

class ServerPingTest {
    @Test
    fun javaGetStatusTest() {
        val result = JavaPing.getStatusBlocking("mc.hypixel.net")
        println(result.description.text)
        println(objectToSectionFormat(result.description.obj))
        assert(result.ping > 0)
    }

    @Test
    fun bedrockGetStatusTest() {
        val result = BedrockPing.getStatusBlocking("asia.easecation.net")
        println(result.description.text)
        println(objectToSectionFormat(result.description.obj))
        assert(result.ping > 0)
    }
}
