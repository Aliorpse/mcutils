// OK, Gemini writes this file, for I know little about DNS lookup.
// Anyone who intends to rewrite this is welcome.

@file:Suppress("MagicNumber", "ReturnCount", "LoopWithTooManyJumpStatements")

package tech.aliorpse.mcutils.internal.util

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.withTimeout
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import tech.aliorpse.mcutils.entity.SrvRecord
import kotlin.random.Random

/**
 * Resolves an SRV record for the given service name using raw DNS via UDP.
 */
internal suspend fun resolveSrvImpl(
    serviceName: String,
    dnsServer: String = "8.8.8.8",
    dnsPort: Int = 53,
    timeoutMillis: Long = 3000L
): SrvRecord? = withDispatchersIO {
    SelectorManager(DispatchersIO).use { selector ->
        val socket = aSocket(selector).udp().bind()

        try {
            withTimeout(timeoutMillis) {
                val serverAddress = InetSocketAddress(dnsServer, dnsPort)
                val queryId = Random.nextInt(0, 65535).toShort()

                // Build DNS Query Packet
                val packetBuffer = Buffer().apply {
                    // Header
                    writeShort(queryId)
                    writeShort(0x0100) // Flags: Standard Query
                    writeShort(1) // QDCOUNT: 1
                    writeShort(0) // ANCOUNT: 0
                    writeShort(0) // NSCOUNT: 0
                    writeShort(0) // ARCOUNT: 0

                    // Question Section
                    writeDomainName(serviceName)
                    writeShort(33) // QTYPE: SRV
                    writeShort(1) // QCLASS: IN
                }

                // Send request
                socket.send(Datagram(packetBuffer, serverAddress))

                // Receive response
                val responseDatagram = socket.receive()
                val responseBytes = responseDatagram.packet.readByteArray()

                // Parse
                val records = parseDnsResponse(responseBytes, queryId)
                selectSrvRecord(records)
            }
        } catch (_: Exception) {
            null
        } finally {
            socket.close()
        }
    }
}

/**
 * Writes a domain name in DNS format to the Buffer.
 */
private fun Buffer.writeDomainName(name: String) {
    val parts = name.split('.')
    for (part in parts) {
        if (part.isNotEmpty()) {
            val bytes = part.toByteArray()
            writeByte(bytes.size.toByte())
            write(bytes)
        }
    }
    writeByte(0)
}

/**
 * Parses the raw DNS response bytes into a list of SrvRecords.
 */
private fun parseDnsResponse(data: ByteArray, expectedId: Short): List<SrvRecord> {
    val parser = DnsParser(data)

    // Header Parsing
    val id = parser.readShort()
    if (id != expectedId) return emptyList()

    val flags = parser.readShort().toInt()
    // Check if it is a response (QR bit) and no error (RCODE)
    // 0x8000 is QR bit. Low 4 bits are RCODE.
    if ((flags and 0x8000) == 0 || (flags and 0x000F) != 0) return emptyList()

    val qdCount = parser.readShort().toInt() and 0xFFFF
    val anCount = parser.readShort().toInt() and 0xFFFF
    parser.readShort() // nsCount
    parser.readShort() // arCount

    // Skip Questions
    repeat(qdCount) {
        parser.skipName()
        parser.skip(4) // Type (2) + Class (2)
    }

    val records = ArrayList<SrvRecord>(anCount)

    // Parse Answers
    repeat(anCount) {
        parser.skipName() // Name
        val type = parser.readShort().toInt()
        parser.readShort() // Class
        parser.readInt() // TTL
        val dataLen = parser.readShort().toInt() and 0xFFFF

        if (type == 33) { // SRV Record
            val priority = parser.readShort().toInt() and 0xFFFF
            val weight = parser.readShort().toInt() and 0xFFFF
            val port = parser.readShort().toInt() and 0xFFFF
            val target = parser.readName()
            records.add(SrvRecord(target, port, priority, weight))
        } else {
            parser.skip(dataLen)
        }
    }

    return records
}

private class DnsParser(private val data: ByteArray) {
    var pos = 0

    fun readByte(): Byte = data[pos++]

    fun readShort(): Short {
        val b1 = data[pos++].toInt() and 0xFF
        val b2 = data[pos++].toInt() and 0xFF
        return ((b1 shl 8) or b2).toShort()
    }

    fun readInt(): Int {
        val b1 = data[pos++].toInt() and 0xFF
        val b2 = data[pos++].toInt() and 0xFF
        val b3 = data[pos++].toInt() and 0xFF
        val b4 = data[pos++].toInt() and 0xFF
        return (b1 shl 24) or (b2 shl 16) or (b3 shl 8) or b4
    }

    fun skip(count: Int) {
        pos += count
    }

    /**
     * Reads a domain name, handling DNS compression pointers (0xC0).
     */
    fun readName(): String {
        val labels = StringBuilder()
        var currentPos = pos
        var jumped = false
        var jumpOffset = -1

        while (true) {
            if (currentPos >= data.size) break
            val len = data[currentPos].toInt() and 0xFF

            if (len == 0) {
                currentPos++
                break
            }

            if ((len and 0xC0) == 0xC0) {
                // Compression pointer
                if (!jumped) {
                    jumpOffset = currentPos + 2 // Position after the pointer
                }
                val b2 = data[currentPos + 1].toInt() and 0xFF
                val offset = ((len and 0x3F) shl 8) or b2
                currentPos = offset
                jumped = true
            } else {
                // Normal label
                currentPos++
                if (labels.isNotEmpty()) labels.append('.')

                val label = data.decodeToString(currentPos, currentPos + len)
                labels.append(label)

                currentPos += len
            }
        }

        pos = if (!jumped) {
            currentPos
        } else {
            jumpOffset
        }

        return labels.toString()
    }

    /**
     * Skips a domain name without allocating strings.
     */
    fun skipName() {
        var currentPos = pos
        while (true) {
            if (currentPos >= data.size) break
            val len = data[currentPos].toInt() and 0xFF

            if (len == 0) {
                pos = currentPos + 1
                return
            }

            if ((len and 0xC0) == 0xC0) {
                // Pointer is 2 bytes, we are done
                pos = currentPos + 2
                return
            }

            // Normal label
            currentPos += 1 + len
        }
    }
}

/**
 * Selects a target server based on SRV priority and weight.
 * Refactored for better readability and reduced nesting.
 */
private fun selectSrvRecord(records: List<SrvRecord>): SrvRecord? {
    if (records.isEmpty()) return null
    if (records.size == 1) return records.first()

    // Sort by Priority (lower is better), then group
    val bestPriorityRecords = records
        .groupBy { it.priority }
        .minByOrNull { it.key } // Get a group with the lowest priority value
        ?.value ?: return null

    if (bestPriorityRecords.size == 1) return bestPriorityRecords.first()

    // Weighted selection within the same priority
    val totalWeight = bestPriorityRecords.sumOf { it.weight }

    if (totalWeight == 0) {
        return bestPriorityRecords.random()
    }

    var randomVal = Random.nextInt(totalWeight + 1)
    for (rec in bestPriorityRecords) {
        randomVal -= rec.weight
        if (randomVal <= 0) {
            return rec
        }
    }

    return bestPriorityRecords.lastOrNull()
}
