package tech.aliorpse.mcutils

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.api.MCServer
import tech.aliorpse.mcutils.api.createMsmpConnection
import tech.aliorpse.mcutils.entity.AllowlistAddedEvent
import kotlin.test.Test

class MsmpTest {
    @Test
    fun `test MSMP`() = runBlocking {
        val connection = MCServer.createMsmpConnection(
            "ws://localhost:25585",
            "n2pQcIG1OQ92jot2xG1M0aw0ZWnrh4F3Z3jw8qRP"
        )

        connection.on<AllowlistAddedEvent> { println(eventCtx) }

        println(
            connection.discover()
        )

        while (isActive) delay(1000)
    }
}
