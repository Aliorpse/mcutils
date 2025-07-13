package tech.aliorpse.mcutils.mojang

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class MojangAPITest {
    @Test
    fun getProfileTest() = runBlocking {
        val result = MojangAPI.getProfileByName("Aliorpse")
        println(result)
        assert(result.name.isNotEmpty())
    }
}