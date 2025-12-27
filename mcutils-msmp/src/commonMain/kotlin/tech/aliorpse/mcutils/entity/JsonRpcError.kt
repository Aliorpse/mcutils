@file:Suppress("MagicNumber")

package tech.aliorpse.mcutils.entity

import kotlinx.serialization.Serializable

@Serializable
public abstract class JsonRpcError {
    public abstract val code: Int
    public abstract val message: String
    public abstract val data: String?

    public companion object
}

public object JsonRpcErrors {

    @Serializable
    public data object ParseError : JsonRpcError() {
        override val code: Int = -32700
        override val message: String = "Parse error"
        override val data: String? = null
    }

    @Serializable
    public data object InvalidRequest : JsonRpcError() {
        override val code: Int = -32600
        override val message: String = "Invalid Request"
        override val data: String? = null
    }

    @Serializable
    public data object MethodNotFound : JsonRpcError() {
        override val code: Int = -32601
        override val message: String = "Method not found"
        override val data: String? = null
    }

    @Serializable
    public data object InvalidParams : JsonRpcError() {
        override val code: Int = -32602
        override val message: String = "Invalid params"
        override val data: String? = null
    }

    @Serializable
    public data object InternalError : JsonRpcError() {
        override val code: Int = -32603
        override val message: String = "Internal error"
        override val data: String? = null
    }

    @Serializable
    public data class UnknownError(
        override val code: Int,
        override val message: String,
        override val data: String? = null
    ) : JsonRpcError()
}

public fun JsonRpcError.Companion.fromCode(code: Int, message: String): JsonRpcError = when (code) {
    -32700 -> JsonRpcErrors.ParseError
    -32600 -> JsonRpcErrors.InvalidRequest
    -32601 -> JsonRpcErrors.MethodNotFound
    -32602 -> JsonRpcErrors.InvalidParams
    -32603 -> JsonRpcErrors.InternalError
    else -> JsonRpcErrors.UnknownError(code, message)
}

public class JsonRpcException(
    public val error: JsonRpcError,
    public val data: String? = null
) : IllegalStateException("RPC Error(${error.code}): ${error.message}${data?.let { ": $it" } ?: ""}")
