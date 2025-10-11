package tech.aliorpse.mcutils.exceptions

import tech.aliorpse.mcutils.model.server.msmp.jsonrpc.JsonRpcError
import tech.aliorpse.mcutils.model.server.msmp.jsonrpc.JsonRpcErrorCode

public class MsmpResponseException(
    public val id: Int?,
    public val code: JsonRpcErrorCode,
    override val message: String,
    public val data: String,
) : Exception("[$code] $message (data=$data)") {
    public constructor(error: JsonRpcError, id: Int? = null) : this(
        id = id,
        code = error.code,
        message = error.message,
        data = error.data,
    )
}
