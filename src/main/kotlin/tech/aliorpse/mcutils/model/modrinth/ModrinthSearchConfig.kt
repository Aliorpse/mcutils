package tech.aliorpse.mcutils.model.modrinth

data class ModrinthSearchConfig(
    var categories: List<String>? = null,
    var versions: List<String>? = null,
    var type: ProjectType? = null,
    var license: String? = null,
    var clientSide: SideSupport? = null,
    var serverSide: SideSupport? = null,
    var openSource: Boolean? = null,
    var author: String? = null,
    var projectId: String? = null,
    var color: String? = null,
    var limit: Int = 10,
    var offset: Int = 0,
    var index: IndexMethod = IndexMethod.RELEVANCE
) {
    private fun MutableList<List<String>>.addFacet(key: String, value: String?) {
        if (value != null) add(listOf("$key:$value"))
    }

    fun buildFacets(): List<List<String>> {
        val result = mutableListOf<List<String>>()

        categories?.let { result += it.map { c -> "categories:$c" } }
        versions?.let { result += it.map { v -> "versions:$v" } }
        result.addFacet("project_type", type?.value)
        result.addFacet("license", license)
        result.addFacet("client_side", clientSide?.value)
        result.addFacet("server_side", serverSide?.value)
        result.addFacet("open_source", openSource?.toString())
        result.addFacet("author", author)
        result.addFacet("project_id", projectId)
        result.addFacet("color", color)

        return result
    }
}

enum class ProjectType(val value: String) {
    MOD("mod"),
    MODPACK("modpack"),
    RESOURCEPACK("resourcepack"),
    SHADER("shader")
}

enum class SideSupport(val value: String) {
    REQUIRED("required"),
    OPTIONAL("optional"),
    UNSUPPORTED("unsupported")
}

enum class IndexMethod(val value: String) {
    RELEVANCE("relevance"),
    DOWNLOADS("downloads"),
    FOLLOWS("follows"),
    NEWEST("newest"),
    UPDATED("updated"),
}
