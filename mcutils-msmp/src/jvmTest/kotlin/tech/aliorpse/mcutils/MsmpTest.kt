package tech.aliorpse.mcutils

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.api.MCServer
import tech.aliorpse.mcutils.api.MsmpConnection
import tech.aliorpse.mcutils.api.createMsmpConnection
import tech.aliorpse.mcutils.entity.AllowlistAddedEvent
import tech.aliorpse.mcutils.entity.AllowlistRemovedEvent
import tech.aliorpse.mcutils.entity.GameruleUpdatedEvent
import tech.aliorpse.mcutils.entity.IpBanAddedEvent
import tech.aliorpse.mcutils.entity.IpBanRemovedEvent
import tech.aliorpse.mcutils.entity.MsmpEvent
import tech.aliorpse.mcutils.entity.OperatorAddedEvent
import tech.aliorpse.mcutils.entity.OperatorRemovedEvent
import tech.aliorpse.mcutils.entity.PlayerJoinedEvent
import tech.aliorpse.mcutils.entity.PlayerLeftEvent
import tech.aliorpse.mcutils.entity.ServerActivityEvent
import tech.aliorpse.mcutils.entity.ServerSavedEvent
import tech.aliorpse.mcutils.entity.ServerSavingEvent
import tech.aliorpse.mcutils.entity.ServerStartedEvent
import tech.aliorpse.mcutils.entity.ServerStatusEvent
import tech.aliorpse.mcutils.entity.ServerStoppingEvent
import tech.aliorpse.mcutils.entity.UnknownMsmpEvent
import tech.aliorpse.mcutils.entity.UserBanAddedEvent
import tech.aliorpse.mcutils.entity.UserBanRemovedEvent
import kotlin.test.Test

class MsmpTest {
    @Test
    fun `test MSMP`(): Unit = runBlocking {
        val connection = MCServer.createMsmpConnection(
            "ws://localhost:25585",
            "n2pQcIG1OQ92jot2xG1M0aw0ZWnrh4F3Z3jw8qRP"
        )

        connection.apply {
            listen<PlayerJoinedEvent>("Join")
            listen<PlayerLeftEvent>("Leave")

            listen<OperatorAddedEvent>("OP+")
            listen<OperatorRemovedEvent>("OP-")

            listen<AllowlistAddedEvent>("Allow+")
            listen<AllowlistRemovedEvent>("Allow-")

            listen<IpBanAddedEvent>("IP Ban+")
            listen<IpBanRemovedEvent>("IP Ban-")
            listen<UserBanAddedEvent>("Ban+")
            listen<UserBanRemovedEvent>("Ban-")

            listen<ServerStartedEvent>("Started")
            listen<ServerStoppingEvent>("Stopping")
            listen<ServerActivityEvent>("Activity")
            listen<ServerSavingEvent>("Saving")
            listen<ServerSavedEvent>("Saved")
            listen<ServerStatusEvent>("Status")

            listen<GameruleUpdatedEvent>("Rule")

            listen<UnknownMsmpEvent>("Unknown")
        }

        while (isActive) delay(1000)
    }
}

private inline fun <reified T : MsmpEvent> MsmpConnection.listen(label: String) =
    on<T> { println("[$label] $this") }
