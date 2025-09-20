package tech.aliorpse.mcutils.modules.modrinth

import io.ktor.client.call.*
import io.ktor.client.request.*
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import tech.aliorpse.mcutils.model.modrinth.project.ModrinthProject
import tech.aliorpse.mcutils.model.modrinth.search.ModrinthSearchConfig
import tech.aliorpse.mcutils.model.modrinth.search.ModrinthSearchResponse
import tech.aliorpse.mcutils.utils.McUtilsHttpClientProvider
import tech.aliorpse.mcutils.utils.withDispatchersIO

/**
 * Provides convenient methods to interact with the Modrinth API.
 */
public object Modrinth {
    private const val API_BASE = "https://api.modrinth.com"

    /**
     * Search for Modrinth projects.
     *
     * @param query Search query.
     * @param config Optional configuration for search parameters.
     * @return [ModrinthSearchResponse] containing the search results.
     */
    @JvmStatic
    @JvmOverloads
    @JvmAsync
    @JvmBlocking
    public suspend fun search(
        query: String,
        config: ModrinthSearchConfig.() -> Unit = {}
    ): ModrinthSearchResponse = withDispatchersIO {
        val cfg = ModrinthSearchConfig().apply(config)
        val facetsStr = cfg.buildFacets()
            .takeIf { it.isNotEmpty() }
            ?.joinToString(prefix = "[", postfix = "]") { inner ->
                inner.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
            }

        McUtilsHttpClientProvider.client.get("$API_BASE/v2/search") {
            parameter("query", query)
            parameter("facets", facetsStr)
            parameter("limit", cfg.limit)
            parameter("offset", cfg.offset)
            parameter("index", cfg.index.value)
        }.body()
    }

    /**
     * Get multiple Modrinth projects by ID or slug.
     */
    @JvmStatic
    @JvmAsync
    @JvmBlocking
    public suspend fun getProjects(ids: List<String>): List<ModrinthProject> = withDispatchersIO {
        val idsStr = ids.joinToString(",", "[", "]") { "\"$it\"" }
        McUtilsHttpClientProvider.client.get("$API_BASE/v2/projects") {
            parameter("ids", idsStr)
        }.body()
    }

    /**
     * Get a single Modrinth project by ID or slug.
     */
    @JvmStatic
    @JvmAsync
    @JvmBlocking
    public suspend fun getProject(id: String): ModrinthProject = withDispatchersIO {
        getProjects(listOf(id))[0]
    }

    /**
     * Get a random list of Modrinth projects.
     *
     * @param count Number of random projects to fetch (max 100).
     */
    @JvmStatic
    @JvmAsync
    @JvmBlocking
    public suspend fun getProjectsRandom(count: Int = 10): List<ModrinthProject> = withDispatchersIO {
        require(count <= 100)
        McUtilsHttpClientProvider.client.get("$API_BASE/v2/projects_random") {
            parameter("count", count)
        }.body()
    }
}
