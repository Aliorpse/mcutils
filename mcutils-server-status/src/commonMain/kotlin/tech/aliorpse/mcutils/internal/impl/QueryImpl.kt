@file:Suppress("MagicNumber")

package tech.aliorpse.mcutils.internal.impl

import io.ktor.network.sockets.ConnectedDatagramSocket
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.discard
import io.ktor.utils.io.core.readShortLittleEndian
import io.ktor.utils.io.core.writeFully
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.Sink
import kotlinx.io.Source
import tech.aliorpse.mcutils.entity.Players
import tech.aliorpse.mcutils.entity.QueryStatus
import tech.aliorpse.mcutils.entity.QueryStatusBasic
import tech.aliorpse.mcutils.entity.QueryStatusFull
import tech.aliorpse.mcutils.entity.Sample
import tech.aliorpse.mcutils.internal.util.Punycode
import tech.aliorpse.mcutils.internal.util.globalSelectorIO

private const val QUERY_SESSION_ID: Int = 0x00070201 // Ciallo ～ (∠·ω< )⌒★
private const val QUERY_SESSION_MASK = 0x0F0F0F0F

internal object QueryImpl {
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
                val newToken = withTimeout(timeout) { socket.queryHandshake() }
                withTimeout(timeout) { socket.getQueryStatus(newToken, isFull) }
            }
        }
    }
}

private suspend fun ConnectedDatagramSocket.getQueryStatus(token: Int, isFull: Boolean): QueryStatus {
    sendQueryPacket(0x00) {
        writeInt(token)
        if (isFull) writeFully(byteArrayOf(0, 0, 0, 0))
    }
    return readQueryPacket(0x00) {
        if (isFull) readStatsFull() else readStatsBasic()
    }
}

private suspend fun ConnectedDatagramSocket.queryHandshake(): Int {
    sendQueryPacket(0x09) {}
    return readQueryPacket(0x09) { readNullTerminatedString() }.toInt()
}

private fun Source.readStatsFull(): QueryStatusFull {
    discard(11)

    val kv = readKVPairs()

    discard(10)

    val players = readPlayerList()
    val plugins = parsePlugins(kv["plugins"])

    return QueryStatusFull(
        description = kv["hostname"].orEmpty(),
        map = kv["map"].orEmpty(),
        players = Players(
            max = kv["maxplayers"]?.toIntOrNull() ?: 0,
            online = kv["numplayers"]?.toIntOrNull() ?: 0,
            sample = players
        ),
        version = kv["version"].orEmpty(),
        plugins = plugins
    )
}

private fun Source.readKVPairs(): Map<String, String> = buildMap {
    while (true) {
        val key = readNullTerminatedString()
        if (key.isEmpty()) break
        val value = readNullTerminatedString()
        put(key, value)
    }
}

private fun Source.readPlayerList(): Set<Sample> = buildSet {
    while (true) {
        val name = readNullTerminatedString()
        if (name.isEmpty()) break
        add(Sample("", name))
    }
}

private fun parsePlugins(raw: String?): Set<String> =
    raw?.takeIf { it.isNotBlank() }
        ?.substringAfter(':', raw)
        ?.splitToSequence(';')
        ?.mapNotNull { it.trim().takeIf { it1 -> it1.isNotEmpty() } }
        ?.toSet()
        ?: emptySet()

private fun Source.readStatsBasic(): QueryStatusBasic {
    val motd = readNullTerminatedString()
    readNullTerminatedString() // GameType (SMP)
    val map = readNullTerminatedString()
    val numPlayers = readNullTerminatedString().toIntOrNull() ?: 0
    val maxPlayers = readNullTerminatedString().toIntOrNull() ?: 0
    readShortLittleEndian() // Port
    readNullTerminatedString() // IP

    return QueryStatusBasic(motd, map, Players(maxPlayers, numPlayers))
}

private suspend fun ConnectedDatagramSocket.sendQueryPacket(
    packetType: Byte,
    payloadBuilder: Sink.() -> Unit
) {
    val packet = buildPacket {
        writeFully(byteArrayOf(0xFE.toByte(), 0xFD.toByte())) // Magic short 0xFEFD
        writeByte(packetType)
        writeInt(QUERY_SESSION_ID)
        payloadBuilder()
    }
    send(Datagram(packet, remoteAddress))
}

private suspend fun <T> ConnectedDatagramSocket.readQueryPacket(
    packetType: Byte,
    readPayload: Source.() -> T
): T {
    val datagram = receive()
    val packet = datagram.packet

    try {
        val packetTypeResp = packet.readByte()
        require(packetTypeResp == packetType) { "Packet type mismatch: expected $packetType, got $packetTypeResp" }

        val session = packet.readInt()
        require(session and QUERY_SESSION_MASK == QUERY_SESSION_ID) {
            "Session ID mismatch: expected ${QUERY_SESSION_ID}, got ${session and QUERY_SESSION_MASK}"
        }

        return packet.readPayload()
    } finally {
        packet.close()
    }
}

private fun Source.readNullTerminatedString(): String {
    var buffer = ByteArray(64)
    var count = 0
    while (true) {
        val b = readByte()
        if (b == 0.toByte()) break
        if (count == buffer.size) {
            buffer = buffer.copyOf(buffer.size * 2)
        }
        buffer[count++] = b
    }
    return buffer.decodeToString(endIndex = count)
}
