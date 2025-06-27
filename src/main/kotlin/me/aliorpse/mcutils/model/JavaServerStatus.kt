package me.aliorpse.mcutils.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

data class JavaServerStatus(
    val description: Description,
    val enforcesSecureChat: Boolean = false,
    val favicon: String,
    val players: Players,
    val version: Version
)


data class Players(
    val max: Int = 0,
    val online: Int = 0,
    val sample: List<Sample>
)

data class Description(
    val text: String
)

// 为什么允许 Description 可以有两种返回格式, 为什么!
class DescriptionDeserializer : JsonDeserializer<Description> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Description {
        return when {
            json.isJsonPrimitive -> {
                val text = json.asString
                Description(text)
            }
            json.isJsonObject -> {
                val obj = json.asJsonObject
                val text = obj.get("text")?.asString ?: ""
                Description(text)
            }
            else -> Description("")
        }
    }
}

data class Sample(
    val id: String,
    val name: String
)

data class Version(
    val name: String,
    val protocol: Int
)
