@file:Suppress("MagicNumber")

package tech.aliorpse.mcutils.internal.impl

import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.core.toByteArray
import io.ktor.utils.io.core.writeFully
import io.ktor.utils.io.readByte
import io.ktor.utils.io.readFully
import io.ktor.utils.io.readLong
import io.ktor.utils.io.writeByte
import io.ktor.utils.io.writePacket
import kotlinx.io.Sink
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import tech.aliorpse.mcutils.annotation.ExperimentalMCUtilsApi
import tech.aliorpse.mcutils.entity.Players
import tech.aliorpse.mcutils.entity.ServerStatus
import tech.aliorpse.mcutils.entity.TextComponent
import tech.aliorpse.mcutils.entity.Version
import tech.aliorpse.mcutils.internal.serializer.TextComponentSerializer
import tech.aliorpse.mcutils.internal.util.Punycode
import tech.aliorpse.mcutils.internal.util.SrvResolver
import tech.aliorpse.mcutils.internal.util.globalSelectorIO
import tech.aliorpse.mcutils.util.toTextComponent
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class ServerListPingImpl {
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalTime::class, ExperimentalMCUtilsApi::class)
    suspend fun getStatus(
        host: String,
        port: Int,
        timeout: Long,
        enableSrv: Boolean,
    ): ServerStatus {
        val asciiRawHost = Punycode.from(host)
        val srvRecord = if (enableSrv) SrvResolver.resolve("_minecraft._tcp.$asciiRawHost") else null

        val connectHost = srvRecord?.target ?: asciiRawHost
        val connectPort = srvRecord?.port ?: port

        aSocket(globalSelectorIO).tcp().connect(connectHost, connectPort) {
            socketTimeout = timeout
            noDelay = true
        }.use { socket ->
            val output = socket.openWriteChannel()
            val input = socket.openReadChannel()

            // Send handshake packet
            output.sendMCPacket(0x00) {
                writeVarInt(-1)
                writeMCString(connectHost)
                writeShort(connectPort.toShort())
                writeVarInt(1)
            }

            // Send status request
            output.sendMCPacket(0x00) {}

            // Read status response
            val jsonString = input.readMCPacket(0x00) { readMCString() }
            val jsonElement = json.parseToJsonElement(jsonString)

            // Send ping request
            val pingStart = Clock.System.now().toEpochMilliseconds()
            output.sendMCPacket(0x01) {
                writeLong(pingStart)
            }

            // Read pong response
            input.readMCPacket(0x01) { readLong() }
            val latency = Clock.System.now().toEpochMilliseconds() - pingStart

            return ServerStatus(
                description = when (val desc = jsonElement.jsonObject["description"]) {
                    is JsonPrimitive -> desc.content.toTextComponent()
                    is JsonObject -> json.decodeFromString(TextComponentSerializer, desc.toString())
                    else -> TextComponent(text = "")
                },
                players = json.decodeFromString(
                    Players.serializer(),
                    jsonElement.jsonObject["players"]?.toString() ?: error("Element `players` missing")
                ),
                version = json.decodeFromString(
                    Version.serializer(),
                    jsonElement.jsonObject["version"]?.toString() ?: error("Element `version` missing")
                ),
                ping = latency,
                secureChatEnforced = jsonElement.jsonObject["enforceSecureChat"]?.jsonPrimitive?.boolean ?: false,
                favicon = jsonElement.jsonObject["favicon"]?.jsonPrimitive?.content,
                srvRecord = "$connectHost:$connectPort"
            )
        }
    }
}

private suspend fun ByteWriteChannel.sendMCPacket(packetId: Int, buildPacketBlock: Sink.() -> Unit) {
    val packet = buildPacket(buildPacketBlock)

    writeVarInt(packet.remaining.toInt() + 1)
    writeVarInt(packetId)
    writePacket(packet)
    flush()
}

private suspend fun <T> ByteReadChannel.readMCPacket(
    packetId: Int,
    readPacketBlock: suspend ByteReadChannel.() -> T
): T {
    val packetLength = readVarInt()
    val packetBytes = ByteArray(packetLength)
    readFully(packetBytes)
    val buffer = ByteReadChannel(packetBytes)
    val packetIdResp = buffer.readVarInt()
    require(packetId == packetIdResp) { "Packet id mismatch: excepted $packetId, got $packetIdResp" }
    return buffer.readPacketBlock()
}

private suspend fun ByteWriteChannel.writeVarInt(value: Int) {
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

private fun Sink.writeVarInt(value: Int) {
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

private suspend fun ByteReadChannel.readVarInt(): Int {
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

private fun Sink.writeMCString(value: String) {
    val bytes = value.toByteArray(Charsets.UTF_8)
    writeVarInt(bytes.size)
    writeFully(bytes)
}

private suspend fun ByteReadChannel.readMCString(): String {
    val length = readVarInt()
    val bytes = ByteArray(length)
    readFully(bytes)
    return bytes.decodeToString()
}
