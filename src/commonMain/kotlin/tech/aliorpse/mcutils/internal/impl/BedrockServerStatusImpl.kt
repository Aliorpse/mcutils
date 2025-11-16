package tech.aliorpse.mcutils.internal.impl

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.io.readByteArray
import tech.aliorpse.mcutils.entity.BedrockServerStatus
import tech.aliorpse.mcutils.entity.GameMode
import tech.aliorpse.mcutils.entity.Players
import tech.aliorpse.mcutils.entity.Version
import tech.aliorpse.mcutils.internal.util.withDispatchersIO
import tech.aliorpse.mcutils.util.toTextComponent
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Suppress("MagicNumber")
internal object BedrockServerStatusImpl {
    @OptIn(ExperimentalTime::class)
    suspend fun getStatus(host: String, port: Int) = withDispatchersIO {
        SelectorManager(currentCoroutineContext()).use { selectorManager ->
            aSocket(selectorManager).udp().connect(InetSocketAddress(host, port)).use { socket ->
                val pingPacket = ByteArray(25) { 0 }.also { it[0] = 0x01 }
                val pingStart = Clock.System.now().toEpochMilliseconds()
                pingStart.toByteArray().copyInto(pingPacket, 1)

                byteArrayOf(
                    0x00, 0xff.toByte(), 0xff.toByte(), 0x00,
                    0xfe.toByte(), 0xfe.toByte(), 0xfe.toByte(), 0xfe.toByte(),
                    0xfd.toByte(), 0xfd.toByte(), 0xfd.toByte(), 0xfd.toByte(),
                    0x12, 0x34, 0x56, 0x78
                ).copyInto(pingPacket, 9)

                socket.send(
                    Datagram(buildPacket { writeFully(pingPacket) }, socket.remoteAddress)
                )

                val data = socket.receive().packet.readByteArray()

                val stringOffset = 35
                val stringBytes = data.drop(stringOffset).toByteArray()
                val fields = stringBytes.decodeToString().split(';')

                require(fields.size >= 9 && fields[0].uppercase() in listOf("MCPE", "MCEE")) {
                    "Invalid Bedrock server response: ${fields.joinToString(";")}"
                }

                BedrockServerStatus(
                    description = fields[1].toTextComponent(),
                    players = Players(
                        online = fields[4].toIntOrNull() ?: 0,
                        max = fields[5].toIntOrNull() ?: 0
                    ),
                    version = Version(
                        name = fields[3],
                        protocol = fields[2].toLongOrNull() ?: 0
                    ),
                    ping = Clock.System.now().toEpochMilliseconds() - pingStart,
                    levelName = fields[7],
                    gameMode = when (fields[8].uppercase()) {
                        "SURVIVAL" -> GameMode.SURVIVAL
                        "CREATIVE" -> GameMode.CREATIVE
                        "ADVENTURE" -> GameMode.ADVENTURE
                        "SPECTATOR" -> GameMode.SPECTATOR
                        else -> GameMode.UNKNOWN
                    },
                    serverUniqueID = fields[6]
                )
            }
        }
    }

    private fun Long.toByteArray(): ByteArray = ByteArray(8) { i ->
        ((this shr (7 - i) * 8) and 0xFF).toByte()
    }
}
