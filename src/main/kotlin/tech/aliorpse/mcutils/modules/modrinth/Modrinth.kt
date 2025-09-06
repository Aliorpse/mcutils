package tech.aliorpse.mcutils.modules.modrinth

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import tech.aliorpse.mcutils.model.modrinth.search.ModrinthSearchConfig
import tech.aliorpse.mcutils.utils.HttpClient
import tech.aliorpse.mcutils.utils.withDispatcherIO

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
    ) = withDispatcherIO {
        val config = ModrinthSearchConfig().apply(config)
        val facets = config.buildFacets()

        val facetsStr = if (facets.isNotEmpty()) {
            listAdapter.toJson(facets)
        } else null

        return@withDispatcherIO HttpClient.modrinthService.search(
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
    @JvmStatic fun searchAsync(
        query: String,
        config: ModrinthSearchConfig.() -> Unit
    ) = CoroutineScope(Dispatchers.IO).future { search(query, config) }



    /**
     * Get a list of [tech.aliorpse.mcutils.model.modrinth.project.ModrinthProject] by their id/slug.
     */
    suspend fun getProjects(projects: List<String>) = withDispatcherIO {
        return@withDispatcherIO HttpClient.modrinthService.getProjects(
            projects.joinToString(
                prefix = "[", postfix = "]"
            ) { "\"$it\"" }
        )
    }
    /**
     * [java.util.concurrent.CompletableFuture] variant of [getProjects].
     */
    @JvmStatic fun getProjectsAsync(projects: List<String>) =
        CoroutineScope(Dispatchers.IO).future { getProjects(projects) }



    /**
     * Single-project variant of [getProjects]
     */
    suspend fun getProject(project: String) = withDispatcherIO {
        return@withDispatcherIO getProjects(listOf(project))[0]
    }
    /**
     * [java.util.concurrent.CompletableFuture] variant of [getProject]
     */
    @JvmStatic fun getProjectAsync(project: String) =
        CoroutineScope(Dispatchers.IO).future { getProject(project) }



    /**
     * Get a list of random [tech.aliorpse.mcutils.model.modrinth.project.ModrinthProject].
     */
    suspend fun getProjectsRandom(count: Int = 10) = withDispatcherIO {
        require(count <= 100)
        return@withDispatcherIO HttpClient.modrinthService.getProjectsRandom(count)
    }
    /**
     * java.util.concurrent.CompletableFuture] variant of [getProjectsRandom].
     */
    @JvmStatic fun getProjectsRandomAsync(count: Int = 10) =
        CoroutineScope(Dispatchers.IO).future { getProjectsRandom(count) }
}
