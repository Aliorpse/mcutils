package tech.aliorpse.mcutils.model.player

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import okio.ByteString.Companion.decodeBase64

/**
 * Custom adapter for converting RawPlayerProfile into a `PlayerProfile` object.
 */
@Suppress("unused")
class PlayerProfileAdapter {
    private val moshi = Moshi.Builder().build()

    @FromJson
    fun fromJson(raw: RawPlayerProfile): PlayerProfile {
        val texturesBase64 = raw.properties.find { it.name == "textures" }!!.value

        val jsonStr = String(texturesBase64.decodeBase64()!!.toByteArray())
        val decoded = moshi.adapter(DecodedTextures::class.java).fromJson(jsonStr)

        val skin = decoded?.textures?.get("SKIN")

        return PlayerProfile(
            id = raw.id,
            name = raw.name,
            legacy = raw.legacy,
            skinUrl = skin?.url,
            capeUrl = decoded?.textures?.get("CAPE")?.url,
            skinModel = SkinModel.from(skin?.metadata?.model)
        )
    }
}
