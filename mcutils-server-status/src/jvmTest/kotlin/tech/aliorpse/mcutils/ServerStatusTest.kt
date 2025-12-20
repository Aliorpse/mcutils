package tech.aliorpse.mcutils

import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.annotation.ExperimentalMCUtilsApi
import tech.aliorpse.mcutils.api.MCServer
import tech.aliorpse.mcutils.api.getQueryBasic
import tech.aliorpse.mcutils.api.getQueryFull
import tech.aliorpse.mcutils.api.getStatus
import kotlin.test.Test

class ServerStatusTest {
    @Test
    fun `fetch java server status by slp`() = runBlocking {
        val result = MCServer.getStatus("hypixel.net")

        assert(result.ping > 0)
        println(result)
    }

    @OptIn(ExperimentalMCUtilsApi::class)
    fun `fetch java server status by query`() = runBlocking {
        val result = MCServer.getQueryFull("localhost")
        val result2 = MCServer.getQueryBasic("localhost")
        println(result)
        println(result2)
    }
}
