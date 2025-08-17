package tech.aliorpse.mcutils.modules.server

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.xbill.DNS.AAAARecord
import org.xbill.DNS.ARecord
import org.xbill.DNS.CNAMERecord
import org.xbill.DNS.Lookup
import org.xbill.DNS.SRVRecord
import org.xbill.DNS.Type
import tech.aliorpse.mcutils.model.server.ColorAdapter
import tech.aliorpse.mcutils.model.server.Description
import tech.aliorpse.mcutils.model.server.DescriptionAdapter
import tech.aliorpse.mcutils.model.server.JavaServerStatus
import tech.aliorpse.mcutils.model.server.MOTDTextComponentAdapter
import tech.aliorpse.mcutils.model.server.Players
import tech.aliorpse.mcutils.model.server.Version
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.IDN
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets

/**
 * Provides functionality to fetch and parse the status of a Java Minecraft server.
 */
@Suppress("MagicNumber", "TooManyFunctions")
object JavaServer {
    private const val HANDSHAKE_PACKET_ID = 0x00
    private const val STATUS_REQUEST_PACKET_ID = 0x00
    private const val STATUS_RESPONSE_PACKET_ID = 0x00
    private const val PING_PACKET_ID = 0x01
    private const val PROTOCOL_VERSION = -1
    private const val NEXT_STATE_STATUS = 1

    private val colorAdapter = ColorAdapter()
    private val motdAdapter = MOTDTextComponentAdapter(colorAdapter)
    private val descAdapter = DescriptionAdapter(motdAdapter)

    internal val moshi: Moshi = Moshi.Builder()
        .add(colorAdapter)
        .add(motdAdapter)
        .add(descAdapter)
        .add(KotlinJsonAdapterFactory())
        .build()

    @JsonClass(generateAdapter = true)
    internal data class RawJavaStatus(
        val description: Description,
        val players: Players,
        val version: Version,
        val favicon: String? = null,
        val enforcesSecureChat: Boolean? = false
    )

    /**
     * Fetch Java server status.
     *
     * @param host Host
     * @param port Port (25565)
     * @param timeout Timeout (2000ms)
     *
     * @throws IOException
     */
    suspend fun getStatus(
        host: String,
        port: Int = 25565,
        timeout: Int = 2000,
    ): JavaServerStatus = withContext(Dispatchers.IO) {
        val asciiHost = IDN.toASCII(host)

        val (srvTarget, srvPort) = resolveSrvRecord(asciiHost) ?: (asciiHost to port)

        val resolvedHost = resolveToIpOrHost(srvTarget) ?: srvTarget

        Socket().use { socket ->
            socket.soTimeout = timeout
            socket.connect(InetSocketAddress(resolvedHost, srvPort), timeout)

            val out = DataOutputStream(socket.getOutputStream())
            val input = DataInputStream(socket.getInputStream())

            sendHandshake(out, asciiHost, srvPort)
            sendStatusRequest(out)

            val jsonStr = readStatusResponse(input)
            val parsed = moshi.adapter(RawJavaStatus::class.java).fromJson(jsonStr)!!

            var ping: Long?
            try {
                val pingStart = System.currentTimeMillis()
                sendPing(out, pingStart)
                readPong(input, pingStart)
                ping = System.currentTimeMillis() - pingStart
            } catch (_: Exception) {
                ping = null
            }

            JavaServerStatus(
                description = parsed.description,
                players = parsed.players,
                version = parsed.version,
                ping = ping,
                enforcesSecureChat = parsed.enforcesSecureChat ?: false,
                favicon = parsed.favicon
            )
        }
    }

    /**
     * Blocking method for [getStatus].
     */
    @JvmStatic
    fun getStatusBlocking(
        host: String,
        port: Int = 25565,
        timeout: Int = 2000,
    ) = runBlocking {
        getStatus(host, port, timeout)
    }

    @Suppress("ReturnCount")
    private fun resolveToIpOrHost(host: String, depth: Int = 5): String? {
        if (depth <= 0) return null

        try {
            // A
            val lookupA = Lookup(host, Type.A)
            lookupA.run()
            val aRecords = lookupA.result.takeIf { lookupA.result == Lookup.SUCCESSFUL }
                ?.let { lookupA.answers.filterIsInstance<ARecord>() }

            if (!aRecords.isNullOrEmpty()) {
                return aRecords[0].address.hostAddress
            }

            // AAAA
            val lookupAAAA = Lookup(host, Type.AAAA)
            lookupAAAA.run()
            val aaaaRecords = lookupAAAA.result.takeIf { lookupAAAA.result == Lookup.SUCCESSFUL }
                ?.let { lookupAAAA.answers.filterIsInstance<AAAARecord>() }

            if (!aaaaRecords.isNullOrEmpty()) {
                return aaaaRecords[0].address.hostAddress
            }

            // CNAME
            val lookupCNAME = Lookup(host, Type.CNAME)
            lookupCNAME.run()
            val cnameRecords = lookupCNAME.result.takeIf { lookupCNAME.result == Lookup.SUCCESSFUL }
                ?.let { lookupCNAME.answers.filterIsInstance<CNAMERecord>() }

            if (!cnameRecords.isNullOrEmpty()) {
                val cnameTarget = cnameRecords[0].target.toString(true)
                if (cnameTarget != host) {
                    return resolveToIpOrHost(cnameTarget, depth - 1)
                }
            }

        } catch (_: Exception) {
            return null
        }
        return null
    }

    private fun resolveSrvRecord(host: String): Pair<String, Int>? {
        return try {
            val lookup = Lookup("_minecraft._tcp.$host", Type.SRV)
            lookup.run()
            if (lookup.result == Lookup.SUCCESSFUL && lookup.answers.isNotEmpty()) {
                val srv = lookup.answers.filterIsInstance<SRVRecord>().minByOrNull { it.priority }
                srv?.let {
                    val target = it.target.toString(true).removeSuffix(".")
                    val port = it.port
                    target to port
                }
            } else {
                null
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
            result = result or ((byte and 0x7F) shl 7 * bytesRead)
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
}
