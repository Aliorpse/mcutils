package tech.aliorpse.mcutils.api

import tech.aliorpse.mcutils.entity.PlayerProfile
import tech.aliorpse.mcutils.internal.impl.PlayerInfoImpl

/**
 * Retrieves a player's profile by name or UUID.
 *
 * UUID can be dashed or undashed.
 *
 * @throws IllegalArgumentException if the input is not a valid username or UUID.
 */
public suspend fun MinecraftPlayer.getProfile(player: String): PlayerProfile = PlayerInfoImpl.getProfile(player)

/**
 * Retrieves the player's UUID by their name.
 *
 * Returns the UUID without dashes.
 */
public suspend fun MinecraftPlayer.getUuid(playerName: String): String = PlayerInfoImpl.getUuid(playerName)
