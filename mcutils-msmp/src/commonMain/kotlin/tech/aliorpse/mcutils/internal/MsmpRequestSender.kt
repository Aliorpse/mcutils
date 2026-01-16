package tech.aliorpse.mcutils.internal

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.aliorpse.mcutils.entity.MsmpRequest

internal class MsmpRequestSender(
    private val connection: DefaultClientWebSocketSession,
    private val batchDelay: Long,
    scope: CoroutineScope
) {
    private val requestChannel = Channel<MsmpRequest>(Channel.UNLIMITED)

    val loopJob: Job = scope.launch {
        runCatching {
            for (firstRequest in requestChannel) {
                val batch = mutableListOf<MsmpRequest>()
                batch.add(firstRequest)

                if (batchDelay > 0) delay(batchDelay)

                var next = requestChannel.tryReceive().getOrNull()
                while (next != null) {
                    batch.add(next)
                    next = requestChannel.tryReceive().getOrNull()
                }

                sendRequest(batch)
            }
        }
    }

    suspend fun send(request: MsmpRequest) {
        requestChannel.send(request)
    }

    private suspend fun sendRequest(requests: List<MsmpRequest>) {
        if (requests.isEmpty()) return

        val payload = if (requests.size == 1) requests[0] else requests
        connection.sendSerialized(payload)
    }

    fun close() {
        requestChannel.close()
        loopJob.cancel()
    }
}
