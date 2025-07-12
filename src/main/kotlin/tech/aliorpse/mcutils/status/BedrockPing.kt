package tech.aliorpse.mcutils.status

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.aliorpse.mcutils.model.BedrockServerStatus
import tech.aliorpse.mcutils.model.Description
import tech.aliorpse.mcutils.model.GameMode
import tech.aliorpse.mcutils.model.Players
import tech.aliorpse.mcutils.model.Version
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
    private const val DEFAULT_PORT = 19132
    private val MAGIC_BYTES = byteArrayOf(
        0x00, 0xFF.toByte(), 0xFF.toByte(), 0x00,
        0xFE.toByte(), 0xFE.toByte(), 0xFE.toByte(), 0xFE.toByte(),
        0xFD.toByte(), 0xFD.toByte(), 0xFD.toByte(), 0xFD.toByte()
    )
    private val CLIENT_ID = byteArrayOf(0x12, 0x34, 0x56, 0x78, 0x00)
    private val CLIENT_GUID = ByteArray(8) { 0 }

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
        port: Int = DEFAULT_PORT,
        timeout: Int = 2000
    ): BedrockServerStatus = withContext(Dispatchers.IO) {
        val asciiHost = IDN.toASCII(host)
        val address = InetAddress.getByName(asciiHost)

        DatagramSocket().use { socket ->
            socket.soTimeout = timeout

            // Construct request packet
            val buffer = ByteBuffer.allocate(1 + 8 + 12 + 5 + 8).order(ByteOrder.BIG_ENDIAN)
            buffer.put(0x01) // Packet ID for ping
            buffer.putLong(System.currentTimeMillis()) // Client sends timestamp
            buffer.put(MAGIC_BYTES)                    // Protocol magic bytes
            buffer.put(CLIENT_ID)                      // Client ID
            buffer.put(CLIENT_GUID)                    // Client GUID (0)

            val sendData = buffer.array()
            val sendPacket = DatagramPacket(sendData, sendData.size, address, port)

            val receiveBuffer = ByteArray(256)
            val receivePacket = DatagramPacket(receiveBuffer, receiveBuffer.size)

            val startTime = System.currentTimeMillis()
            socket.send(sendPacket)
            socket.receive(receivePacket)
            val endTime = System.currentTimeMillis()

            val data = receivePacket.data
            // Packet ID for response is 0x1C
            if (data.isEmpty() || data[0] != 0x1C.toByte()) {
                throw IOException("Invalid response packet, expected packet ID 0x1C")
            }

            // Server info string starts at byte offset 33
            val serverInfoRaw = data.copyOfRange(33, receivePacket.length)
            val serverInfoStr = serverInfoRaw.toString(StandardCharsets.UTF_8).trim('\u0000')

            // Split the info string by ';' and parse fields
            val parts = serverInfoStr.split(";")
            if (parts.size < 9) {
                throw IOException("Incomplete server info data, expected at least 9 parts")
            }

            val protocol = parts[2].toIntOrNull() ?: 0
            val online = parts[4].toIntOrNull() ?: 0
            val max = parts[5].toIntOrNull() ?: 0

            BedrockServerStatus(
                description = Description(parts[1]),
                players = Players(
                    max = max,
                    online = online,
                    sample = emptyList()
                ),
                version = Version(
                    name = parts[3],
                    protocol = protocol
                ),
                ping = endTime - startTime,
                levelName = parts[7],
                gameMode = GameMode.valueOf(parts[8].uppercase()),
                serverUniqueID = parts[6]
            )
        }
    }
}
