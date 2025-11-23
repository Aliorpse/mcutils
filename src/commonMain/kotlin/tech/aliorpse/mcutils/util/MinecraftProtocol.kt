package tech.aliorpse.mcutils.util

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.core.toByteArray
import io.ktor.utils.io.core.writeFully
import io.ktor.utils.io.readByte
import io.ktor.utils.io.readFully
import io.ktor.utils.io.writeByte
import io.ktor.utils.io.writePacket
import kotlinx.io.Sink
import love.forte.plugin.suspendtrans.annotation.JsPromise
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import tech.aliorpse.mcutils.annotation.ExperimentalMCUtilsApi
import tech.aliorpse.mcutils.internal.impl.ServerStatusImpl

/**
 * Examples see [ServerStatusImpl.getStatus]
 */
@Suppress("MagicNumber")
@ExperimentalMCUtilsApi
@JsPromise
@JvmAsync
@JvmBlocking
public object MinecraftProtocol {
    public suspend fun ByteWriteChannel.sendMCPacket(packetId: Int, buildPacketBlock: Sink.() -> Unit) {
        val packet = buildPacket(buildPacketBlock)

        writeVarInt(packet.remaining.toInt() + 1)
        writeVarInt(packetId)
        writePacket(packet)
        flush()
    }

    public suspend fun <T> ByteReadChannel.readMCPacket(
        packetId: Int,
        readPacketBlock: suspend ByteReadChannel.() -> T
    ): T {
        val packetLength = readVarInt()
        val packetBytes = ByteArray(packetLength)
        readFully(packetBytes)
        val buffer = ByteReadChannel(packetBytes)
        val packetIdResp = buffer.readVarInt()
        require(packetId == packetIdResp) { "Received packet id $packetIdResp, but expected $packetId" }
        return buffer.readPacketBlock()
    }

    public suspend fun ByteWriteChannel.writeVarInt(value: Int) {
        var v = value
        while (true) {
            if (v and 0x7F.inv() == 0) {
                writeByte(v.toByte())
                return
            }
            writeByte(((v and 0x7F) or 0x80).toByte())
            v = v ushr 7
        }
    }

    public fun Sink.writeVarInt(value: Int) {
        var v = value
        while (true) {
            if (v and 0x7F.inv() == 0) {
                write(ByteArray(1) { v.toByte() })
                flush()
                return
            }
            write(ByteArray(1) { ((v and 0x7F) or 0x80).toByte() })
            v = v ushr 7
        }
    }

    public suspend fun ByteReadChannel.readVarInt(): Int {
        var numRead = 0
        var result = 0
        var read: Int
        do {
            read = readByte().toInt()
            val value = read and 0b01111111
            result = result or (value shl 7 * numRead)
            numRead++
            if (numRead > 5) error("VarInt too big")
        } while (read and 0b10000000 != 0)
        return result
    }

    public fun Sink.writeMCString(value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        writeVarInt(bytes.size)
        writeFully(bytes)
    }

    public suspend fun ByteReadChannel.readMCString(): String {
        val length = readVarInt()
        val bytes = ByteArray(length)
        readFully(bytes)
        return bytes.decodeToString()
    }
}
