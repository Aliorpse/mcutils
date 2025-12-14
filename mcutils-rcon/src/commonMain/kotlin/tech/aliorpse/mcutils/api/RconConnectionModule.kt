package tech.aliorpse.mcutils.api

import io.ktor.network.sockets.aSocket
import tech.aliorpse.mcutils.internal.impl.RconConnectionImpl
import tech.aliorpse.mcutils.internal.util.Punycode
import tech.aliorpse.mcutils.internal.util.globalSelectorIO

/**
 * Create a RCON connection to a Minecraft server.
 *
 * @param password RCON password for authentication.
 */
public suspend fun MinecraftServer.createRconConnection(
    host: String,
    port: Int = 25575,
    password: String,
    timeout: Long = 10000L,
): RconConnection {
    val impl = RconConnectionImpl(
        aSocket(globalSelectorIO).tcp()
            .connect(Punycode.from(host), port) { socketTimeout = timeout }
    )
    impl.authenticate(password)
    return RconConnection(impl)
}

public class RconConnection internal constructor(
    private val impl: RconConnectionImpl
) : AutoCloseable {
    /**
     * Execute the given command.
     *
     * - Commands run sequentially; concurrent calls are queued until the previous one finishes.
     * - The command **must not** start with a leading "/".
     * - The command size must be **less than** 1447 bytes.
     * - On server-side failure, the response will be:
     *   "Error executing: $command ($message)".
     */
    public suspend fun execute(command: String): String = impl.execute(command)

    override fun close(): Unit = impl.connection.close()
}
