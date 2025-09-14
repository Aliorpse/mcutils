package tech.aliorpse.mcutils.player

import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.modules.player.Player
import tech.aliorpse.mcutils.utils.McUtilsHttpClient
import kotlin.test.Test

class PlayerProfileTest {
    /**
     * Simple test for getting player's profile.
     */
    @Test
    fun getProfileTest() {
        McUtilsHttpClient.init(CIO)
        val result = runBlocking { Player.getProfile("ec042e1200ac4a249cc83eb1fab0bd88") }
        println(result)
        assert(result.name.isNotEmpty())
    }
}
