@file:Suppress("MagicNumber")

package tech.aliorpse.mcutils.internal.impl

import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readByte
import io.ktor.utils.io.readFully
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.withTimeout
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import tech.aliorpse.mcutils.entity.Players
import tech.aliorpse.mcutils.entity.ServerStatus
import tech.aliorpse.mcutils.entity.TextComponent
import tech.aliorpse.mcutils.entity.Version
import tech.aliorpse.mcutils.internal.util.Punycode
import tech.aliorpse.mcutils.internal.util.SrvResolver
import tech.aliorpse.mcutils.internal.util.globalSelectorIO
import tech.aliorpse.mcutils.util.fromJson
import tech.aliorpse.mcutils.util.fromString
import kotlin.time.Clock

internal object ServerListPingImpl {
    private val json = Json { ignoreUnknownKeys = true }

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
            val output = socket.openWriteChannel(autoFlush = true)
            val input = socket.openReadChannel()

            return withTimeout(timeout) {
                // 1. Handshake Packet (ID 0x00)
                output.sendMCPacket(0x00) {
                    writeVarInt(-1) // Protocol version
                    writeMCString(connectHost)
                    writeShort(connectPort.toShort())
                    writeVarInt(1) // Next state: 1 (Status)
                }

                // 2. Request Packet (ID 0x00)
                output.sendMCPacket(0x00) {}

                // 3. Read Status Response (ID 0x00)
                val jsonString = input.readMCPacket(0x00) {
                    readMCString()
                }

                // Parse JSON
                val jsonElement = json.parseToJsonElement(jsonString)

                // 4. Ping Packet (ID 0x01)
                val pingStart = Clock.System.now().toEpochMilliseconds()
                output.sendMCPacket(0x01) {
                    writeLong(pingStart)
                }

                // 5. Read Pong Response (ID 0x01)
                input.readMCPacket(0x01) {
                    readLong()
                }
                val latency = Clock.System.now().toEpochMilliseconds() - pingStart

                return@withTimeout ServerStatus(
                    description = when (val desc = jsonElement.jsonObject["description"]) {
                        is JsonPrimitive -> TextComponent.fromString(desc.content)
                        is JsonObject -> TextComponent.fromJson(desc)
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
}

private suspend fun ByteWriteChannel.sendMCPacket(
    packetId: Int,
    block: Sink.() -> Unit
) {
    val packetData = Buffer()
    packetData.writeVarInt(packetId)
    packetData.block()

    val length = packetData.size

    val finalBuffer = Buffer()
    finalBuffer.writeVarInt(length.toInt())
    finalBuffer.write(packetData, packetData.size)

    writeFully(finalBuffer.readByteArray())
    flush()
}

private suspend fun <T> ByteReadChannel.readMCPacket(
    expectedPacketId: Int,
    block: Source.() -> T
): T {
    val length = readVarInt() // Read directly from a channel
    val data = ByteArray(length)
    readFully(data) // Read exact bytes

    val buffer = Buffer()
    buffer.write(data)

    try {
        val actualPacketId = buffer.readVarInt()
        require(actualPacketId == expectedPacketId) {
            "Packet ID mismatch: expected $expectedPacketId, got $actualPacketId"
        }
        return buffer.block()
    } finally {
        buffer.close()
    }
}

private fun Sink.writeVarInt(value: Int) {
    var v = value
    while (true) {
        if ((v and 0x7F.inv()) == 0) {
            writeByte(v.toByte())
            return
        }
        writeByte(((v and 0x7F) or 0x80).toByte())
        v = v ushr 7
    }
}

@Suppress("DuplicatedCode")
private fun Source.readVarInt(): Int {
    var numRead = 0
    var result = 0
    var read: Int
    do {
        read = readByte().toInt()
        val value = read and 0b01111111
        result = result or (value shl (7 * numRead))
        numRead++
        if (numRead > 5) error("VarInt is too big")
    } while ((read and 0b10000000) != 0)
    return result
}

/**
 * Necessary because it'd need to read the packet length before buffering the rest.
 */
@Suppress("DuplicatedCode")
private suspend fun ByteReadChannel.readVarInt(): Int {
    var numRead = 0
    var result = 0
    var read: Int
    do {
        read = readByte().toInt()
        val value = read and 0b01111111
        result = result or (value shl (7 * numRead))
        numRead++
        if (numRead > 5) error("VarInt is too big")
    } while ((read and 0b10000000) != 0)
    return result
}

private fun Sink.writeMCString(value: String) {
    val bytes = value.encodeToByteArray()
    writeVarInt(bytes.size)
    write(bytes)
}

private fun Source.readMCString(): String {
    val length = readVarInt()
    return readString(byteCount = length.toLong())
}
