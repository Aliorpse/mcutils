package tech.aliorpse.mcutils.modules.modrinth

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import tech.aliorpse.mcutils.model.modrinth.search.ModrinthSearchConfig
import tech.aliorpse.mcutils.utils.HttpClient

object Modrinth {
    private val moshi = Moshi.Builder().build()

    private val listType = Types.newParameterizedType(
        List::class.java,
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    private val listAdapter = moshi.adapter<List<List<String>>>(listType)

    /**
     * Search from Modrinth.
     */
    suspend fun search(
        query: String,
        config: ModrinthSearchConfig.() -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        val config = ModrinthSearchConfig().apply(config)
        val facets = config.buildFacets()

        val facetsStr = if (facets.isNotEmpty()) {
            listAdapter.toJson(facets)
        } else null

        return@withContext HttpClient.modrinthService.search(
            query = query,
            facets = facetsStr,
            limit = config.limit,
            offset = config.offset,
            index = config.index.value
        )
    }

    /**
     * [java.util.concurrent.CompletableFuture] variant of [search].
     */
    fun searchAsync(
        query: String,
        config: ModrinthSearchConfig.() -> Unit
    ) = CoroutineScope(Dispatchers.IO).future { search(query, config) }

    /**
     * Get a [tech.aliorpse.mcutils.model.modrinth.project.ModrinthProject] by its id/slug.
     */
    suspend fun getProject(project: String) = withContext(Dispatchers.IO) {
        return@withContext HttpClient.modrinthService.getProject(project)
    }

    /**
     * [java.util.concurrent.CompletableFuture] variant of [getProject].
     */
    fun getProjectAsync(project: String) = CoroutineScope(Dispatchers.IO).future { getProject(project) }
}
