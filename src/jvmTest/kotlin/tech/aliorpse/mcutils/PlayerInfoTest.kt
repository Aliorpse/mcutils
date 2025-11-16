package tech.aliorpse.mcutils

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import tech.aliorpse.mcutils.api.MCPlayer
import kotlin.test.Test

class PlayerInfoTest {
    @Test
    fun `get player uuid by name`() = runBlocking {
        val id = MCPlayer.getUuid("Aliorpse")

        assert(id == "ec042e1200ac4a249cc83eb1fab0bd88")
    }

    @Test
    fun `get player profile`() = runBlocking {
        val profileByNameDeferred = async { MCPlayer.getProfile("Aliorpse") }
        val profileByUuidDeferred = async { MCPlayer.getProfile("ec042e1200ac4a249cc83eb1fab0bd88") }

        val profileByName = profileByNameDeferred.await()
        val profileByUuid = profileByUuidDeferred.await()

        assert(profileByName == profileByUuid)
        assert(profileByName.name == "Aliorpse")
        println(profileByName)
    }
}
