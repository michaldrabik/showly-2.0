package com.michaldrabik.network.trakt.api

import com.michaldrabik.network.Config
import com.michaldrabik.network.trakt.model.Comment
import com.michaldrabik.network.trakt.model.Episode
import com.michaldrabik.network.trakt.model.OAuthResponse
import com.michaldrabik.network.trakt.model.SearchResult
import com.michaldrabik.network.trakt.model.Season
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.network.trakt.model.ShowResult
import com.michaldrabik.network.trakt.model.SyncItem
import com.michaldrabik.network.trakt.model.request.OAuthRefreshRequest
import com.michaldrabik.network.trakt.model.request.OAuthRequest
import com.michaldrabik.network.trakt.model.request.OAuthRevokeRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TraktService {

  @GET("shows/{traktId}?extended=full")
  suspend fun fetchShow(@Path("traktId") traktId: Long): Show

  @GET("shows/trending?extended=full&limit=${Config.TRAKT_TRENDING_SHOWS_LIMIT}")
  suspend fun fetchTrendingShows(): List<ShowResult>

  @GET("shows/anticipated?extended=full&limit=${Config.TRAKT_ANTICIPATED_SHOWS_LIMIT}")
  suspend fun fetchAnticipatedShows(): List<ShowResult>

  @GET("shows/{traktId}/related?extended=full&limit=${Config.TRAKT_RELATED_SHOWS_LIMIT}")
  suspend fun fetchRelatedShows(@Path("traktId") traktId: Long): List<Show>

  @GET("shows/{traktId}/next_episode?extended=full")
  suspend fun fetchNextEpisode(@Path("traktId") traktId: Long): Response<Episode>

  @GET("search/show?extended=full&limit=${Config.TRAKT_SEARCH_LIMIT}")
  suspend fun fetchSearchResults(@Query("query") queryText: String): List<SearchResult>

  @GET("shows/{traktId}/seasons?extended=full,episodes")
  suspend fun fetchSeasons(@Path("traktId") traktId: Long): List<Season>

  @GET("shows/{traktId}/seasons/{seasonNumber}/episodes/{episodeNumber}/comments?limit=30&extended=full")
  suspend fun fetchEpisodeComments(
    @Path("traktId") traktId: Long,
    @Path("seasonNumber") seasonNumber: Int,
    @Path("episodeNumber") episodeNumber: Int
  ): List<Comment>

  //Auth

  @POST("oauth/token")
  suspend fun fetchOAuthToken(@Body request: OAuthRequest): OAuthResponse

  @POST("oauth/token")
  suspend fun refreshOAuthToken(@Body request: OAuthRefreshRequest): OAuthResponse

  @POST("oauth/revoke")
  suspend fun revokeOAuthToken(@Body request: OAuthRevokeRequest): Response<Any>

  //Sync

  @GET("sync/watched/shows?extended=full")
  suspend fun fetchSyncWatched(@Header("Authorization") authToken: String): List<SyncItem>


  @GET("sync/watchlist?extended=full")
  suspend fun fetchSyncWatchlist(@Header("Authorization") authToken: String): List<SyncItem>
}