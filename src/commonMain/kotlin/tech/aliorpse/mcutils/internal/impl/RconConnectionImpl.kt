@file:Suppress("MagicNumber")

package tech.aliorpse.mcutils.internal.impl

import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.readByte
import io.ktor.utils.io.readFully
import io.ktor.utils.io.writeByte
import io.ktor.utils.io.writePacket
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Sink
import kotlinx.io.writeString
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement

@OptIn(ExperimentalAtomicApi::class)
internal class RconConnectionImpl(
    internal val connection: Socket
) {
    private val idCounter = AtomicInt(1)
    private val mutex = Mutex()

    private val output = connection.openWriteChannel()
    private val input = connection.openReadChannel()

    suspend fun execute(command: String): String = mutex.withLock {
        val roundId = idCounter.fetchAndIncrement()
        output.sendRconPacket(roundId, 2) { writeString(command) }
        return input.readRconResponse(roundId, 0)
    }

    suspend fun authenticate(password: String) {
        output.sendRconPacket(0, 3) { writeString(password) }
        input.readRconPacket(0, 2)
    }
}

private suspend fun ByteWriteChannel.sendRconPacket(
    packetId: Int,
    packetType: Int,
    buildPacketBlock: Sink.() -> Unit
) {
    val payload = buildPacket {
        buildPacketBlock()
        writeByte(0x00)
    }

    require(payload.remaining.toInt() <= 1447) { "Payload too large for RCON" }

    val length = 4 + 4 + payload.remaining.toInt() + 1

    val packet = buildPacket {
        writeIntLittleEndian(length)
        writeIntLittleEndian(packetId)
        writeIntLittleEndian(packetType)
        writePacket(payload)
        writeByte(0x00)
    }

    writePacket(packet)
    flush()
}

private suspend fun ByteWriteChannel.writeIntLittleEndian(value: Int) {
    writeByte((value and 0xFF).toByte())
    writeByte(((value shr 8) and 0xFF).toByte())
    writeByte(((value shr 16) and 0xFF).toByte())
    writeByte(((value shr 24) and 0xFF).toByte())
}

private data class RconPacket(
    val type: Int,
    val payload: String
)

private suspend fun ByteReadChannel.readRconPacket(packetId: Int, packetType: Int): RconPacket {
    val length = readIntLittleEndian()

    val packetIdResp = readIntLittleEndian()
    val packetTypeResp = readIntLittleEndian()

    val remaining = length - 8
    require(remaining >= 0) { "Invalid RCON length: $length" }

    val payloadBytes = ByteArray(remaining)
    readFully(payloadBytes)

    val payloadResp = payloadBytes.takeWhile { it.toInt() != 0 }.toByteArray().decodeToString()

    if (packetIdResp == -1 && packetTypeResp == 2) error("RCON Authentication failed")
    require(packetIdResp == packetId) { "Packet id mismatch: expected $packetId, got $packetIdResp" }
    require(packetTypeResp == packetType) { "Packet type mismatch: expected $packetType, got $packetTypeResp" }

    return RconPacket(packetTypeResp, payloadResp)
}

private suspend fun ByteReadChannel.readRconResponse(packetId: Int, packetType: Int): String {
    val builder = StringBuilder()

    while (true) {
        val response = readRconPacket(packetId, packetType)
        builder.append(response.payload)

        if (response.payload.length < 4096) break
    }

    return builder.toString()
}

private suspend fun ByteReadChannel.readIntLittleEndian(): Int {
    val b1 = readByte().toInt() and 0xFF
    val b2 = readByte().toInt() and 0xFF
    val b3 = readByte().toInt() and 0xFF
    val b4 = readByte().toInt() and 0xFF
    return b1 or (b2 shl 8) or (b3 shl 16) or (b4 shl 24)
}
