package tech.aliorpse.mcutils

import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.api.*
import kotlin.test.Test

class ServerStatusTest {
    @Test
    fun `fetch java server status by slp`() = runBlocking {
        val result = MCServer.getStatus("wdsj.net")

        assert(result.ping > 0)
        println(result)
    }

    @Test
    fun `fetch java server status by query`() = runBlocking {
        val result = MCServer.getQueryFull("localhost")
        val result2 = MCServer.getQueryBasic("localhost")
        println(result)
        println(result2)
    }

    @Test
    fun `test srv resolving`() = runBlocking {
        println(MCServer.resolveSrv("wdsj.net"))
    }
}
