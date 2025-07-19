package tech.aliorpse.mcutils.model.mojang

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import okio.ByteString.Companion.decodeBase64

/**
 * A custom adapter for converting JSON data into a `PlayerProfile` object.
 *
 * This adapter implements `@FromJson` to parse player profile data, including UUID, name,
 * textures (skin, cape), and model type, from Mojang session server's JSON response.
 */
class PlayerProfileAdapter {

    private val moshi = Moshi.Builder().build()

    @FromJson
    fun fromJson(raw: RawPlayerProfile): PlayerProfile {
        val texturesBase64 = raw.properties.find { it.name == "textures" }?.value

        var skinUrl: String? = null
        var capeUrl: String? = null
        var model = SkinModel.CLASSIC

        if (texturesBase64 != null) {
            try {
                val jsonStr = String(texturesBase64.decodeBase64()!!.toByteArray())
                val decoded = moshi.adapter(DecodedTextures::class.java).fromJson(jsonStr)
                val skin = decoded?.textures?.get("SKIN")
                val cape = decoded?.textures?.get("CAPE")
                skinUrl = skin?.url
                capeUrl = cape?.url
                model = SkinModel.from(skin?.metadata?.model)
            } catch (_: Exception) {
            }
        }

        return PlayerProfile(
            id = raw.id,
            name = raw.name,
            legacy = raw.legacy,
            skinUrl = skinUrl,
            capeUrl = capeUrl,
            skinModel = model
        )
    }
}
