package tech.aliorpse.mcutils.internal.impl

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readLong
import kotlinx.coroutines.currentCoroutineContext
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
import tech.aliorpse.mcutils.internal.util.withDispatchersIO
import tech.aliorpse.mcutils.util.MinecraftProtocol.readMCPacket
import tech.aliorpse.mcutils.util.MinecraftProtocol.readMCString
import tech.aliorpse.mcutils.util.MinecraftProtocol.sendMCPacket
import tech.aliorpse.mcutils.util.MinecraftProtocol.writeMCString
import tech.aliorpse.mcutils.util.MinecraftProtocol.writeVarInt
import tech.aliorpse.mcutils.util.toTextComponent
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Suppress("MagicNumber")
internal object ServerStatusImpl {
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalTime::class, ExperimentalMCUtilsApi::class)
    suspend fun getStatus(
        host: String,
        port: Int,
        timeout: Long,
        enableSrv: Boolean,
    ) = withDispatchersIO {
        val asciiRawHost = Punycode.from(host)
        val srvRecord = if (enableSrv) SrvResolver.resolve("_minecraft._tcp.$asciiRawHost") else null

        val connectHost = srvRecord?.target ?: asciiRawHost
        val connectPort = srvRecord?.port ?: port

        SelectorManager(currentCoroutineContext()).use { selector ->
            aSocket(selector).tcp().connect(connectHost, connectPort) {
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

                ServerStatus(
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
}
