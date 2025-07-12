package tech.aliorpse.mcutils.status

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class JavaPingTest {
    @Test
    fun getStatusTest() = runBlocking {
        val result = JavaPing.getStatus("wdsj.net")
        println(result)

        assert(result.ping > 0)
    }
}