package tech.aliorpse.mcutils.modules.modrinth

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import tech.aliorpse.mcutils.model.modrinth.project.ModrinthProject
import tech.aliorpse.mcutils.model.modrinth.search.ModrinthSearchResponse

internal interface ModrinthService {
    @GET("/v2/search")
    suspend fun search(
        @Query("query") query: String,
        @Query("facets") facets: String?,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("index") index: String,
    ): ModrinthSearchResponse

    @GET("/v2/project/{idOrSlug}")
    suspend fun getProject(
        @Path("idOrSlug") idOrSlug: String,
    ): ModrinthProject
}
