package tech.aliorpse.mcutils.modules.modrinth

import retrofit2.http.GET
import retrofit2.http.Query
import tech.aliorpse.mcutils.model.modrinth.ModrinthSearchResponse

internal interface ModrinthService {
    @GET("/v2/search")
    suspend fun search(
        @Query("query") query: String,
        @Query("facets") facets: String?,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("index") index: String,
    ): ModrinthSearchResponse
}
