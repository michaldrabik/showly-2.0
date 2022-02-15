package com.michaldrabik.data_remote.trakt.api.service

import com.michaldrabik.data_remote.trakt.model.RatingResultEpisode
import com.michaldrabik.data_remote.trakt.model.RatingResultMovie
import com.michaldrabik.data_remote.trakt.model.RatingResultSeason
import com.michaldrabik.data_remote.trakt.model.RatingResultShow
import com.michaldrabik.data_remote.trakt.model.SyncExportRequest
import com.michaldrabik.data_remote.trakt.model.SyncExportResult
import com.michaldrabik.data_remote.trakt.model.SyncItem
import com.michaldrabik.data_remote.trakt.model.request.RatingRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TraktSyncService {
  @GET("sync/watched/{type}")
  suspend fun fetchSyncWatched(
    @Header("Authorization") authToken: String,
    @Path("type") type: String,
    @Query("extended") extended: String?
  ): List<SyncItem>

  @GET("sync/watchlist/{type}?extended=full")
  suspend fun fetchSyncWatchlist(
    @Header("Authorization") authToken: String,
    @Path("type") type: String,
    @Query("page") page: Int? = null,
    @Query("limit") limit: Int? = null
  ): List<SyncItem>

  @POST("sync/watchlist")
  suspend fun postSyncWatchlist(
    @Header("Authorization") authToken: String,
    @Body request: SyncExportRequest
  ): SyncExportResult

  @POST("sync/history")
  suspend fun postSyncWatched(
    @Header("Authorization") authToken: String,
    @Body request: SyncExportRequest
  ): SyncExportResult

  @POST("sync/watchlist/remove")
  suspend fun deleteWatchlist(
    @Header("Authorization") authToken: String,
    @Body request: SyncExportRequest
  ): SyncExportResult

  @POST("sync/history/remove")
  suspend fun deleteHistory(
    @Header("Authorization") authToken: String,
    @Body request: SyncExportRequest
  ): SyncExportResult

  @POST("sync/ratings")
  suspend fun postRating(
    @Header("Authorization") authToken: String,
    @Body request: RatingRequest
  ): Response<Any>

  @POST("sync/ratings/remove")
  suspend fun postRemoveRating(
    @Header("Authorization") authToken: String,
    @Body request: RatingRequest
  ): Response<Any>

  @GET("sync/ratings/shows")
  suspend fun fetchShowsRatings(
    @Header("Authorization") authToken: String
  ): List<RatingResultShow>

  @GET("sync/ratings/movies")
  suspend fun fetchMoviesRatings(
    @Header("Authorization") authToken: String
  ): List<RatingResultMovie>

  @GET("sync/ratings/episodes")
  suspend fun fetchEpisodesRatings(
    @Header("Authorization") authToken: String
  ): List<RatingResultEpisode>

  @GET("sync/ratings/seasons")
  suspend fun fetchSeasonsRatings(
    @Header("Authorization") authToken: String
  ): List<RatingResultSeason>
}
