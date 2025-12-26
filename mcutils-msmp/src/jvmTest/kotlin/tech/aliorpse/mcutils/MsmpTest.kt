package tech.aliorpse.mcutils

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.annotation.ExperimentalMCUtilsApi
import tech.aliorpse.mcutils.api.MCServer
import tech.aliorpse.mcutils.api.createMsmpConnection
import tech.aliorpse.mcutils.entity.MessageDto

class MsmpTest {
    @OptIn(DelicateCoroutinesApi::class, ExperimentalMCUtilsApi::class)
    fun `test MSMP`(): Unit = runBlocking {
        MCServer.createMsmpConnection(
            "ws://localhost:25585",
            "n2pQcIG1OQ92jot2xG1M0aw0ZWnrh4F3Z3jw8qRP"
        ).use { conn ->
            GlobalScope.launch { conn.eventFlow.collect { println(it) } }

            println(conn.server.status())
            conn.gamerules.set("send_command_feedback", true)
            conn.serverSettings.allowFlight.set(true)
            conn.server.sendMessage(
                conn.players.get(),
                MessageDto(
                    literal = conn.banList.get().joinToString(", ") { it.player.name!! }
                ),
            )

            while (isActive) delay(1000)
        }
    }
}
