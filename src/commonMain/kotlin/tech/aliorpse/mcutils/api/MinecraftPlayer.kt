package tech.aliorpse.mcutils.api

import love.forte.plugin.suspendtrans.annotation.JsPromise
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import tech.aliorpse.mcutils.entity.PlayerProfile
import tech.aliorpse.mcutils.internal.impl.PlayerInfoImpl
import kotlin.jvm.JvmStatic

public object MinecraftPlayer {
    /**
     * Retrieves a player's profile by name or UUID.
     *
     * UUID can be dashed or undashed.
     *
     * @throws IllegalArgumentException if the input is not a valid username or UUID.
     */
    @JsPromise
    @JvmAsync
    @JvmBlocking
    @JvmStatic
    public suspend fun getProfile(player: String): PlayerProfile = PlayerInfoImpl.getProfile(player)

    @JsPromise
    @JvmAsync
    @JvmBlocking
    @JvmStatic
    /**
     * Retrieves the player's UUID by their name.
     *
     * Returns the UUID without dashes.
     */
    public suspend fun getUuid(playerName: String): String = PlayerInfoImpl.getUuid(playerName)
}
