@file:Suppress("MagicNumber")

package tech.aliorpse.mcutils.internal.impl

import io.ktor.network.sockets.ConnectedDatagramSocket
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import io.ktor.utils.io.readByte
import io.ktor.utils.io.readFully
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.Sink
import kotlinx.io.readByteArray
import tech.aliorpse.mcutils.entity.Players
import tech.aliorpse.mcutils.entity.QueryStatus
import tech.aliorpse.mcutils.entity.QueryStatusBasic
import tech.aliorpse.mcutils.entity.QueryStatusFull
import tech.aliorpse.mcutils.entity.Sample
import tech.aliorpse.mcutils.internal.util.Punycode
import tech.aliorpse.mcutils.internal.util.globalSelectorIO

private const val QUERY_SESSION_ID: Int = 0x00070201 // Ciallo ～ (∠·ω< )⌒★
private const val QUERY_SESSION_MASK = 0x0F0F0F0F

internal class QueryImpl {
    suspend fun getQuery(
        host: String,
        port: Int,
        timeout: Long,
        isFull: Boolean,
    ): QueryStatus {
        val socket = withTimeout(timeout) {
            aSocket(globalSelectorIO).udp().connect(
                InetSocketAddress(Punycode.from(host), port)
            )
        }

        socket.use { socket ->
            val token = withTimeout(timeout) { socket.queryHandshake() }

            return withTimeoutOrNull(timeout) {
                socket.getQueryStatus(token, isFull)
            } ?: run {
                val token = withTimeout(timeout) { socket.queryHandshake() }
                withTimeout(timeout) { socket.getQueryStatus(token, isFull) }
            }
        }
    }
}

private suspend fun ConnectedDatagramSocket.getQueryStatus(token: Int, isFull: Boolean): QueryStatus {
    sendQueryPacket(0x00) {
        writeBigEndianInt(token)
        if (isFull) writeFully(byteArrayOf(0, 0, 0, 0))
    }
    return readQueryStatus(isFull)
}

private suspend fun ConnectedDatagramSocket.queryHandshake(): Int {
    sendQueryPacket(0x09) {}
    return readQueryPacket(0x09) { readNullTerminatedString() }.toInt()
}

private suspend fun ConnectedDatagramSocket.readQueryStatus(isFull: Boolean): QueryStatus {
    return readQueryPacket(0x00) {
        if (isFull) {
            readFully(ByteArray(11)) // Meaningless padding

            val kv = mutableMapOf<String, String>()
            while (true) {
                val key = readNullTerminatedString()
                if (key.isEmpty()) break
                val value = readNullTerminatedString()
                kv[key] = value
            }

            readFully(ByteArray(10)) // Meaningless padding

            val players = mutableSetOf<Sample>()
            while (true) {
                val playerName = readNullTerminatedString()
                if (playerName.isEmpty()) break
                players += Sample("", playerName)
            }

            val rawPlugins = kv["plugins"] ?: ""
            val plugins = run {
                if (rawPlugins.isEmpty()) {
                    emptySet()
                } else {
                    val parts = rawPlugins.split(":", limit = 2)
                    val pluginPart = if (parts.size == 2) parts[1] else parts[0]
                    pluginPart.split(";").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                }
            }

            QueryStatusFull(
                kv["hostname"] ?: "",
                kv["map"] ?: "",
                Players(
                    kv["maxplayers"]?.toIntOrNull() ?: 0,
                    kv["numplayers"]?.toIntOrNull() ?: 0,
                    players.toSet()
                ),
                kv["version"] ?: "",
                plugins
            )
        } else {
            val motd = readNullTerminatedString()
            readNullTerminatedString() // GameType == "SMP"
            val map = readNullTerminatedString()
            val numPlayers = readNullTerminatedString().toInt()
            val maxPlayers = readNullTerminatedString().toInt()
            readLittleEndianUShort() // Port
            readNullTerminatedString() // IP

            QueryStatusBasic(motd, map, Players(maxPlayers, numPlayers))
        }
    }
}

private suspend fun ConnectedDatagramSocket.sendQueryPacket(
    packetType: Byte,
    payloadBuilder: Sink.() -> Unit
) {
    val packet = buildPacket {
        writeFully(byteArrayOf(0xFE.toByte(), 0xFD.toByte())) // Magic short 0xFEFD
        writeByte(packetType)
        writeBigEndianInt(QUERY_SESSION_ID)
        payloadBuilder()
    }
    send(Datagram(packet, remoteAddress))
}

private suspend fun <T> ConnectedDatagramSocket.readQueryPacket(
    packetType: Byte,
    readPayload: suspend ByteReadChannel.() -> T
): T {
    val datagram = receive()
    val buffer = ByteReadChannel(datagram.packet.readByteArray())

    val packetTypeResp = buffer.readByte()
    if (packetTypeResp != packetType) error("Packet type mismatch: expected $packetType, got $packetTypeResp")

    val session = buffer.readBigEndianInt()
    if ((session and QUERY_SESSION_MASK) != (QUERY_SESSION_ID and QUERY_SESSION_MASK)) {
        error("Session ID mismatch")
    }

    return buffer.readPayload()
}

private fun Sink.writeBigEndianInt(value: Int) {
    val b = byteArrayOf(
        ((value ushr 24) and 0xFF).toByte(),
        ((value ushr 16) and 0xFF).toByte(),
        ((value ushr 8) and 0xFF).toByte(),
        (value and 0xFF).toByte(),
    )
    writeFully(b)
}

private fun Sink.writeLittleEndianUShort(value: UShort) {
    val v = value.toInt() and 0xFFFF
    val b = byteArrayOf(
        (v and 0xFF).toByte(),
        ((v ushr 8) and 0xFF).toByte(),
    )
    writeFully(b)
}

private suspend fun ByteReadChannel.readBigEndianInt(): Int {
    val b = ByteArray(4)
    readFully(b)
    return ((b[0].toInt() and 0xFF) shl 24) or
        ((b[1].toInt() and 0xFF) shl 16) or
        ((b[2].toInt() and 0xFF) shl 8) or
        (b[3].toInt() and 0xFF)
}

private suspend fun ByteReadChannel.readLittleEndianUShort(): UShort {
    val b = ByteArray(2)
    readFully(b)
    val v = (b[0].toInt() and 0xFF) or ((b[1].toInt() and 0xFF) shl 8)
    return v.toUShort()
}

private suspend fun ByteReadChannel.readNullTerminatedString(): String {
    val bytes = mutableListOf<Byte>()
    while (true) {
        val b = readByte()
        if (b == 0.toByte()) break
        bytes += b
    }
    return bytes.toByteArray().decodeToString()
}
