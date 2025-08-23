package tech.aliorpse.mcutils.player

import tech.aliorpse.mcutils.modules.player.Player
import kotlin.test.Test

class PlayerProfileTest {
    /**
     * Simple test for getting player's profile.
     */
    @Test
    fun getProfileTest() {
        val result = Player.getProfileBlocking("ec042e1200ac4a249cc83eb1fab0bd88")
        println(result)
        assert(result.name.isNotEmpty())
    }
}
