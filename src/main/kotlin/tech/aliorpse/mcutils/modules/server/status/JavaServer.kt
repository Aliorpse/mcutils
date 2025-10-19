package tech.aliorpse.mcutils.modules.server.status

import kotlinx.serialization.json.Json
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import tech.aliorpse.mcutils.config.McUtilsConfig
import tech.aliorpse.mcutils.exceptions.ServerStatusException
import tech.aliorpse.mcutils.model.server.status.JavaServerStatus
import tech.aliorpse.mcutils.model.server.status.JavaServerStatusSerializer
import tech.aliorpse.mcutils.utils.HostPort
import tech.aliorpse.mcutils.utils.withDispatchersIO
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.OutputStream
import java.net.IDN
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets

@Suppress("MagicNumber", "TooManyFunctions")
public object JavaServer {
    private const val HANDSHAKE_PACKET_ID = 0x00
    private const val STATUS_REQUEST_PACKET_ID = 0x00
    private const val STATUS_RESPONSE_PACKET_ID = 0x00
    private const val PING_PACKET_ID = 0x01
    private const val PROTOCOL_VERSION = -1
    private const val NEXT_STATE_STATUS = 1

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches the Java server status.
     *
     * @param host The server host.
     * @param port The server port (default 25565).
     * @param connectionTimeout Connection timeout for establishing the socket (default 2000ms).
     * @param readTimeout Read timeout applied to socket SO_TIMEOUT for response reads (default 4000ms).
     * @throws ServerStatusException When response is malformed or does not meet expectations.
     */
    @JvmStatic
    @JvmOverloads
    @JvmAsync
    @JvmBlocking
    public suspend fun getStatus(
        host: String,
        port: Int = 25565,
        connectionTimeout: Int = 2000,
        readTimeout: Int = 4000
    ): JavaServerStatus = withDispatchersIO {
        val asciiHost = IDN.toASCII(host)
        val (srvTarget, srvPort) = McUtilsConfig.dns.srvResolver(asciiHost) ?: (asciiHost to port)

        Socket().use { socket ->
            socket.soTimeout = readTimeout
            socket.connect(InetSocketAddress(srvTarget, srvPort), connectionTimeout)

            val out = DataOutputStream(socket.getOutputStream())
            val input = DataInputStream(socket.getInputStream())

            sendHandshake(out, asciiHost, srvPort)
            sendStatusRequest(out)

            val jsonStr = readStatusResponse(input)
            val parsed = json.decodeFromString(JavaServerStatusSerializer, jsonStr)

            val ping = runCatching {
                val pingStart = System.currentTimeMillis()
                sendPing(out, pingStart)
                readPong(input, pingStart)
                System.currentTimeMillis() - pingStart
            }.getOrNull()

            JavaServerStatus(
                description = parsed.description,
                players = parsed.players,
                version = parsed.version,
                ping = ping,
                enforcesSecureChat = parsed.enforcesSecureChat,
                favicon = parsed.favicon
            )
        }
    }

    /**
     * Fetches the Java server status.
     *
     * @param hostPort As it named.
     * @param connectionTimeout Connection timeout for establishing the socket (default 2000ms).
     * @param readTimeout Read timeout applied to socket SO_TIMEOUT for response reads (default 4000ms)
     * @return [JavaServerStatus] representing the server's status.
     * @throws IllegalArgumentException If host is null.
     * @throws ServerStatusException When response is malformed or does not meet expectations.
     */
    @JvmStatic
    @JvmOverloads
    @JvmAsync
    @JvmBlocking
    public suspend fun getStatus(
        hostPort: HostPort,
        connectionTimeout: Int = 2000,
        readTimeout: Int = 4000
    ): JavaServerStatus = getStatus(
        hostPort.host ?: throw IllegalArgumentException("The host is null."),
        hostPort.port ?: 25565,
        connectionTimeout,
        readTimeout,
    )

    private fun sendHandshake(out: DataOutputStream, host: String, port: Int) {
        val payload = ByteArrayOutputStream()
        val data = DataOutputStream(payload)
        writeVarInt(data, PROTOCOL_VERSION)
        writeString(data, host)
        data.writeShort(port)
        writeVarInt(data, NEXT_STATE_STATUS)

        val body = payload.toByteArray()
        writeVarInt(out, body.size + 1)
        writeVarInt(out, HANDSHAKE_PACKET_ID)
        out.write(body)
    }

    private fun sendStatusRequest(out: DataOutputStream) {
        writeVarInt(out, 1)
        writeVarInt(out, STATUS_REQUEST_PACKET_ID)
    }

    private fun sendPing(out: DataOutputStream, payload: Long) {
        writeVarInt(out, 9)
        writeVarInt(out, PING_PACKET_ID)
        out.writeLong(payload)
    }

    private fun readStatusResponse(input: DataInputStream): String {
        readVarInt(input)
        val packetId = readVarInt(input)
        if (packetId != STATUS_RESPONSE_PACKET_ID)
            throw ServerStatusException("Unexpected status response packet ID: $packetId")
        val jsonLength = readVarInt(input)
        val jsonData = ByteArray(jsonLength)
        input.readFully(jsonData)
        return String(jsonData, StandardCharsets.UTF_8)
    }

    private fun readPong(input: DataInputStream, expectedPayload: Long) {
        readVarInt(input)
        val packetId = readVarInt(input)
        if (packetId != PING_PACKET_ID)
            throw ServerStatusException("Unexpected pong packet ID: $packetId")
        val payload = input.readLong()
        if (payload != expectedPayload)
            throw ServerStatusException("Pong payload mismatch: expected $expectedPayload, got $payload")
    }

    private fun writeVarInt(out: OutputStream, value: Int) {
        var v = value
        while (true) {
            var temp = v and 0x7F
            v = v ushr 7
            if (v != 0) temp = temp or 0x80
            out.write(temp)
            if (v == 0) break
        }
    }

    private fun writeVarInt(out: DataOutputStream, value: Int) = writeVarInt(out as OutputStream, value)

    private fun readVarInt(input: DataInputStream): Int {
        var result = 0
        var bytesRead = 0
        while (true) {
            val byte = input.readByte().toInt()
            result = result or ((byte and 0x7F) shl 7 * bytesRead)
            bytesRead++
            if (bytesRead > 5) throw ServerStatusException("VarInt too big")
            if (byte and 0x80 == 0) break
        }
        return result
    }

    private fun writeString(out: DataOutputStream, str: String) {
        val bytes = str.toByteArray(StandardCharsets.UTF_8)
        writeVarInt(out, bytes.size)
        out.write(bytes)
    }
}
