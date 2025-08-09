package tech.aliorpse.mcutils.player

import tech.aliorpse.mcutils.modules.player.Player
import kotlin.test.Test

class PlayerProfileTest {
    /**
     * Simple test for getting player's profile.
     */
    @Test
    fun getProfileTest() {
        val result = Player.getProfileByNameBlocking("Aliorpse")
        println(result)
        assert(result.name.isNotEmpty())
    }
}
