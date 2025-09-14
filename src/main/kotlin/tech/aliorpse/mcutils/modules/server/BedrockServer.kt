package tech.aliorpse.mcutils.modules.server

import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import tech.aliorpse.mcutils.model.server.BedrockServerStatus
import tech.aliorpse.mcutils.model.server.GameMode
import tech.aliorpse.mcutils.model.server.Players
import tech.aliorpse.mcutils.model.server.Version
import tech.aliorpse.mcutils.utils.toTextComponent
import tech.aliorpse.mcutils.utils.withDispatchersIO
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.IDN
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

@Suppress("MagicNumber")
public object BedrockServer {
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
     * Fetches the status of a Bedrock server.
     *
     * @param host The server hostname or IP.
     * @param port The server port (default 19132).
     * @param timeout Timeout in milliseconds (default 2000ms).
     * @return A [BedrockServerStatus] containing the parsed server information.
     * @throws IOException If the server response is invalid or timed out.
     */
    @JvmStatic
    @JvmOverloads
    @JvmAsync
    @JvmBlocking
    public suspend fun getStatus(
        host: String,
        port: Int = 19132,
        timeout: Int = 2000
    ): BedrockServerStatus = withDispatchersIO {
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
                throw IOException("Invalid response: expected packet ID 0x1C, got ${data.getOrNull(0)}")
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

            val protocol = parts[2].toLongOrNull() ?: 0
            val online = parts[4].toIntOrNull() ?: 0
            val max = parts[5].toIntOrNull() ?: 0
            val gameMode = runCatching { GameMode.valueOf(parts[8].uppercase()) }.getOrDefault(GameMode.UNKNOWN)

            return@withDispatchersIO BedrockServerStatus(
                description = parts[1].toTextComponent(),
                players = Players(online = online, max = max, sample = emptyList()),
                version = Version(name = parts[3], protocol = protocol),
                ping = end - start,
                levelName = parts[7],
                gameMode = gameMode,
                serverUniqueID = parts[6]
            )
        }
    }
}
