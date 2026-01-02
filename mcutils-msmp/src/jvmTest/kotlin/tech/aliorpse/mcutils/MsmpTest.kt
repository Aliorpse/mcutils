package tech.aliorpse.mcutils

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.annotation.ExperimentalMCUtilsApi
import tech.aliorpse.mcutils.api.MCServer
import tech.aliorpse.mcutils.api.msmpClient
import tech.aliorpse.mcutils.api.registry.allowList
import tech.aliorpse.mcutils.api.registry.banList
import tech.aliorpse.mcutils.api.registry.gamerules
import tech.aliorpse.mcutils.api.registry.ipBanList
import tech.aliorpse.mcutils.api.registry.players
import tech.aliorpse.mcutils.api.registry.server
import tech.aliorpse.mcutils.api.registry.serverSettings
import tech.aliorpse.mcutils.entity.MessageDto
import kotlin.test.Test

class MsmpTest {
    @OptIn(DelicateCoroutinesApi::class, ExperimentalMCUtilsApi::class)
    @Test
    fun `test MSMP`(): Unit = runBlocking {
        MCServer.msmpClient(
            "ws://localhost:25585",
            "n2pQcIG1OQ92jot2xG1M0aw0ZWnrh4F3Z3jw8qRP"
        ).use { client ->
            client.startConnection()

            GlobalScope.launch { client.eventFlow.collect { println(it) } }
            GlobalScope.launch { client.stateFlow.collect { println(it) } }

            println(client.server.status())
            client.gamerules.set("send_command_feedback", true)
            client.serverSettings.allowFlight.set(true)
            client.server.sendMessage(
                client.players.snapshot(),
                MessageDto(
                    literal = client.banList.snapshot().joinToString(", ") { it.player.name!! }
                ),
            )

            delay(1000)

            println(client.players.snapshot())
            println(client.banList.snapshot())
            println(client.ipBanList.snapshot())
            println(client.allowList.snapshot())
            println(client.gamerules.snapshot())

            client.await()
        }
    }
}
