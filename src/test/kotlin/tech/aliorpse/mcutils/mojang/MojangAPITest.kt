package tech.aliorpse.mcutils.mojang

import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.module.mojang.MojangAPI
import kotlin.test.Test

class MojangAPITest {
    /**
     * Simple test for getting player's profile.
     */
    @Test
    fun getProfileTest() = runBlocking {
        val result = MojangAPI.getProfileByName("Aliorpse")
        println(result)
        assert(result.name.isNotEmpty())
    }
}
