package tech.aliorpse.mcutils.api.model

public sealed class ConnectionState {
    public class Disconnected(public val error: Throwable? = null) : ConnectionState()
    public object Connected : ConnectionState()
}
