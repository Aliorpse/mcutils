package me.aliorpse.mcutils.status

import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.aliorpse.mcutils.model.Description
import me.aliorpse.mcutils.model.DescriptionDeserializer
import me.aliorpse.mcutils.model.JavaServerStatus
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets

@Suppress("MagicNumber")
object JavaPing {
    private const val HANDSHAKE_PACKET_ID = 0x00
    private const val STATUS_REQUEST_PACKET_ID = 0x00
    private const val STATUS_RESPONSE_PACKET_ID = 0x00
    private const val PING_PACKET_ID = 0x01
    private const val PROTOCOL_VERSION = -1
    private const val NEXT_STATE_STATUS = 1

    private val gson = GsonBuilder()
        .registerTypeAdapter(Description::class.java, DescriptionDeserializer())
        .create()

    /**
     * 获取 MC Java 服务器状态
     *
     * @param host 主机地址
     * @param port 端口 (25565)
     * @param timeout 超时时间 (2000ms)
     * @param enableSrv 启用 Srv 解析 (true)
     *
     * @throws IOException
     */
    suspend fun getStatus(
        host: String,
        port: Int = 25565,
        timeout: Int = 2000,
        enableSrv: Boolean = true
    ): StatusResult = withContext(Dispatchers.IO) {
        // Unicode 域名
        val asciiHost = java.net.IDN.toASCII(host)

        // SRV 记录
        val (resolvedHost, resolvedPort) = if (enableSrv) {
            resolveSrvRecord(asciiHost) ?: (asciiHost to port)
        } else {
            asciiHost to port
        }

        Socket().use { socket ->
            socket.soTimeout = timeout
            socket.connect(InetSocketAddress(resolvedHost, resolvedPort), timeout)

            val out = DataOutputStream(socket.getOutputStream())
            val input = DataInputStream(socket.getInputStream())

            // 握手请求
            sendHandshake(out, asciiHost, resolvedPort)
            sendStatusRequest(out)

            // 解析 JSON
            val json = readStatusResponse(input)
            val status = gson.fromJson(json, JavaServerStatus::class.java)

            val pingStart = System.currentTimeMillis()
            sendPing(out, pingStart)
            readPong(input, pingStart)
            val ping = System.currentTimeMillis() - pingStart

            StatusResult(status, ping)
        }
    }

    private fun resolveSrvRecord(host: String): Pair<String, Int>? {
        return try {
            val dnsContext = javax.naming.directory.InitialDirContext()
            val records = dnsContext.getAttributes(
                "_minecraft._tcp.$host",
                arrayOf("SRV")
            ).get("SRV")?.toString()?.lines()?.firstOrNull()

            records?.let {
                val parts = it.trim().split("\\s+".toRegex())
                if (parts.size == 4) {
                    val target = parts[3].removeSuffix(".")
                    val port = parts[2].toInt()
                    target to port
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun sendHandshake(out: DataOutputStream, host: String, port: Int) {
        val handshakePayload = ByteArrayOutputStream()
        val handshakeData = DataOutputStream(handshakePayload)

        writeVarInt(handshakeData, PROTOCOL_VERSION)
        writeString(handshakeData, host)
        handshakeData.writeShort(port)
        writeVarInt(handshakeData, NEXT_STATE_STATUS)

        val body = handshakePayload.toByteArray()
        writeVarInt(out, body.size + 1) // 总长度
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
        if (packetId != STATUS_RESPONSE_PACKET_ID) {
            throw IOException("Unexpected status response packet ID: $packetId")
        }
        val jsonLength = readVarInt(input)
        val jsonData = ByteArray(jsonLength)
        input.readFully(jsonData)
        return String(jsonData, StandardCharsets.UTF_8)
    }

    private fun readPong(input: DataInputStream, expectedPayload: Long) {
        readVarInt(input)
        val packetId = readVarInt(input)
        if (packetId != PING_PACKET_ID) {
            throw IOException("Unexpected pong packet ID: $packetId")
        }
        val payload = input.readLong()
        if (payload != expectedPayload) {
            throw IOException("Pong payload mismatch: expected $expectedPayload, got $payload")
        }
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

    private fun writeVarInt(out: DataOutputStream, value: Int) {
        writeVarInt(out as OutputStream, value)
    }

    private fun readVarInt(input: DataInputStream): Int {
        var result = 0
        var bytesRead = 0
        while (true) {
            val byte = input.readByte().toInt()
            result = result or ((byte and 0x7F) shl (7 * bytesRead))
            bytesRead++
            if (bytesRead > 5) throw IOException("VarInt too big")
            if (byte and 0x80 == 0) break
        }
        return result
    }

    private fun writeString(out: DataOutputStream, str: String) {
        val bytes = str.toByteArray(StandardCharsets.UTF_8)
        writeVarInt(out, bytes.size)
        out.write(bytes)
    }

    data class StatusResult(
        val status: JavaServerStatus,
        val ping: Long
    )
}
