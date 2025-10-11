package tech.aliorpse.mcutils.modules.server.msmp.api

import tech.aliorpse.mcutils.annotations.InternalMcUtilsApi
import tech.aliorpse.mcutils.model.server.msmp.common.PlayerDto
import tech.aliorpse.mcutils.modules.server.msmp.MsmpConnection

@OptIn(InternalMcUtilsApi::class)
public class AllowlistModule(private val client: MsmpConnection) {
    public suspend fun get(): List<PlayerDto> = client.call(
        PlayerDto.serializer(),
        "allowlist"
    )

    public suspend fun clear(): List<PlayerDto> = client.call(
        PlayerDto.serializer(),
        "allowlist/clear"
    )

    public suspend fun set(vararg players: String): List<PlayerDto> = client.call(
        PlayerDto.serializer(),
        "allowlist/set",
        players.map { it.toPlayerDto() },
    )

    public suspend fun add(vararg players: String): List<PlayerDto> = client.call(
        PlayerDto.serializer(),
        "allowlist/add",
        players.map { it.toPlayerDto() },
    )

    public suspend fun remove(vararg players: String): List<PlayerDto> = client.call(
        PlayerDto.serializer(),
        "allowlist/remove",
        players.map { it.toPlayerDto() },
    )

    @Suppress("MagicNumber")
    private fun String.toPlayerDto(): PlayerDto {
        val normalized = replace("-", "")
        return when (normalized.length) {
            32 -> PlayerDto(this, null)
            in 3..16 -> PlayerDto(null, normalized)
            else -> error("Invalid identifier: $normalized")
        }
    }
}
