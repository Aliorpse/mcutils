package tech.aliorpse.mcutils.status

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import tech.aliorpse.mcutils.model.status.BedrockServerStatus
import tech.aliorpse.mcutils.model.status.Description
import tech.aliorpse.mcutils.model.status.GameMode
import tech.aliorpse.mcutils.model.status.Players
import tech.aliorpse.mcutils.model.status.Version
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.IDN
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

@Suppress("MagicNumber")
object BedrockPing {
    private val MAGIC_BYTES = byteArrayOf(
        0x00, 0xFF.toByte(), 0xFF.toByte(), 0x00,
        0xFE.toByte(), 0xFE.toByte(), 0xFE.toByte(), 0xFE.toByte(),
        0xFD.toByte(), 0xFD.toByte(), 0xFD.toByte(), 0xFD.toByte()
    )

    private val CLIENT_ID = byteArrayOf(0x12, 0x34, 0x56, 0x78, 0x00)
    private val CLIENT_GUID = ByteArray(8) { 0 }

    private const val PACKET_ID_UNCONNECTED_PONG = 0x1C.toByte()
    private const val PACKET_ID_UNCONNECTED_PING = 0x01.toByte()
    private const val BUFFER_SIZE = 256
    private const val MIN_EXPECTED_PARTS = 9
    private const val SERVER_INFO_OFFSET = 33

    /**
     * Fetch Bedrock server status.
     *
     * @param host Host
     * @param port Port (19132)
     * @param timeout Timeout (2000ms)
     *
     * @throws IOException
     */
    suspend fun getStatus(
        host: String,
        port: Int = 19132,
        timeout: Int = 2000
    ): BedrockServerStatus = withContext(Dispatchers.IO) {
        val asciiHost = IDN.toASCII(host)
        val address = InetAddress.getByName(asciiHost)

        DatagramSocket().use { socket ->
            socket.soTimeout = timeout

            val requestBuffer = ByteBuffer.allocate(1 + 8 + MAGIC_BYTES.size + CLIENT_ID.size + CLIENT_GUID.size)
                .order(ByteOrder.BIG_ENDIAN)
            requestBuffer.put(PACKET_ID_UNCONNECTED_PING)
            requestBuffer.putLong(System.currentTimeMillis())
            requestBuffer.put(MAGIC_BYTES)
            requestBuffer.put(CLIENT_ID)
            requestBuffer.put(CLIENT_GUID)

            val sendPacket = DatagramPacket(requestBuffer.array(), requestBuffer.position(), address, port)
            val receiveBuffer = ByteArray(BUFFER_SIZE)
            val receivePacket = DatagramPacket(receiveBuffer, BUFFER_SIZE)

            val start = System.currentTimeMillis()
            socket.send(sendPacket)
            socket.receive(receivePacket)
            val end = System.currentTimeMillis()

            val data = receivePacket.data
            if (data.isEmpty() || data[0] != PACKET_ID_UNCONNECTED_PONG) {
                throw IOException("Invalid response: expected packet ID 0x1C, got ${data[0]}")
            }

            if (receivePacket.length <= SERVER_INFO_OFFSET) {
                throw IOException("Received packet too short to contain server info")
            }

            val infoRaw = data.copyOfRange(SERVER_INFO_OFFSET, receivePacket.length)
            val infoStr = infoRaw.toString(StandardCharsets.UTF_8).trimEnd('\u0000')

            val parts = infoStr.split(";")
            if (parts.size < MIN_EXPECTED_PARTS) {
                throw IOException("Malformed response: expected at least $MIN_EXPECTED_PARTS parts, got ${parts.size}")
            }

            val protocol = parts[2].toIntOrNull() ?: 0
            val online = parts[4].toIntOrNull() ?: 0
            val max = parts[5].toIntOrNull() ?: 0
            val gameMode = runCatching { GameMode.valueOf(parts[8].uppercase()) }.getOrDefault(GameMode.UNKNOWN)

            return@withContext BedrockServerStatus(
                description = Description(parts[1]),
                players = Players(online = online, max = max, sample = emptyList()),
                version = Version(name = parts[3], protocol = protocol),
                ping = end - start,
                levelName = parts[7],
                gameMode = gameMode,
                serverUniqueID = parts[6]
            )
        }
    }

    /**
     * Blocking method for [getStatus].
     */
    @JvmStatic
    fun getStatusBlocking(
        host: String,
        port: Int = 19132,
        timeout: Int = 2000
    ) = runBlocking {
        getStatus(host, port, timeout)
    }
}
