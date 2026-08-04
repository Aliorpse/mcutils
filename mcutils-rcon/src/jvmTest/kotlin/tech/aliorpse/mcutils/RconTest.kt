package tech.aliorpse.mcutils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.api.MCServer
import tech.aliorpse.mcutils.api.createRconConnection
import tech.aliorpse.mcutils.api.model.ConnectionState
import kotlin.test.Test
import kotlin.time.Duration.Companion.hours

class RconTest {
    @Test
    fun `test RCON`() = runBlocking {
        MCServer.createRconConnection("localhost", password = "mcutilsTest")
            .use { conn ->
                println(
                    conn.execute("help")
                )

                conn.connectionState.first { it is ConnectionState.Disconnected }
                println("Disconnected")
            }

        delay(1.hours)
    }
}
