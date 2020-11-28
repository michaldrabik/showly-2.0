package com.michaldrabik.network.trakt.api

import com.michaldrabik.network.Config
import com.michaldrabik.network.trakt.model.ActorsResponse
import com.michaldrabik.network.trakt.model.Comment
import com.michaldrabik.network.trakt.model.Episode
import com.michaldrabik.network.trakt.model.HiddenItem
import com.michaldrabik.network.trakt.model.Movie
import com.michaldrabik.network.trakt.model.MovieResult
import com.michaldrabik.network.trakt.model.OAuthResponse
import com.michaldrabik.network.trakt.model.RatingResultEpisode
import com.michaldrabik.network.trakt.model.RatingResultShow
import com.michaldrabik.network.trakt.model.SearchResult
import com.michaldrabik.network.trakt.model.Season
import com.michaldrabik.network.trakt.model.SeasonTranslation
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.network.trakt.model.ShowResult
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.network.trakt.model.SyncExportResult
import com.michaldrabik.network.trakt.model.SyncItem
import com.michaldrabik.network.trakt.model.Translation
import com.michaldrabik.network.trakt.model.User
import com.michaldrabik.network.trakt.model.request.OAuthRefreshRequest
import com.michaldrabik.network.trakt.model.request.OAuthRequest
import com.michaldrabik.network.trakt.model.request.OAuthRevokeRequest
import com.michaldrabik.network.trakt.model.request.RatingRequest
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

  @GET("movies/{traktId}?extended=full")
  suspend fun fetchMovie(@Path("traktId") traktId: Long): Movie

  @GET("shows/popular?extended=full&limit=${Config.TRAKT_POPULAR_SHOWS_LIMIT}")
  suspend fun fetchPopularShows(
    @Query("genres") genres: String
  ): List<Show>

  @GET("movies/popular?extended=full&limit=${Config.TRAKT_POPULAR_MOVIES_LIMIT}")
  suspend fun fetchPopularMovies(
    @Query("genres") genres: String
  ): List<Movie>

  @GET("shows/trending?extended=full&limit=${Config.TRAKT_TRENDING_SHOWS_LIMIT}")
  suspend fun fetchTrendingShows(
    @Query("genres") genres: String
  ): List<ShowResult>

  @GET("movies/trending?extended=full&limit=${Config.TRAKT_TRENDING_MOVIES_LIMIT}")
  suspend fun fetchTrendingMovies(
    @Query("genres") genres: String
  ): List<MovieResult>

  @GET("shows/anticipated?extended=full&limit=${Config.TRAKT_ANTICIPATED_SHOWS_LIMIT}")
  suspend fun fetchAnticipatedShows(
    @Query("genres") genres: String
  ): List<ShowResult>

  @GET("movies/anticipated?extended=full&limit=${Config.TRAKT_ANTICIPATED_MOVIES_LIMIT}")
  suspend fun fetchAnticipatedMovies(
    @Query("genres") genres: String
  ): List<MovieResult>

  @GET("shows/{traktId}/related?extended=full&limit=${Config.TRAKT_RELATED_SHOWS_LIMIT}")
  suspend fun fetchRelatedShows(@Path("traktId") traktId: Long): List<Show>

  @GET("shows/{traktId}/next_episode?extended=full")
  suspend fun fetchNextEpisode(@Path("traktId") traktId: Long): Response<Episode>

  @GET("search/show?extended=full&limit=${Config.TRAKT_SEARCH_LIMIT}")
  suspend fun fetchSearchResults(@Query("query") queryText: String): List<SearchResult>

  @GET("shows/{traktId}/seasons?extended=full,episodes")
  suspend fun fetchSeasons(@Path("traktId") traktId: Long): List<Season>

  @GET("shows/{traktId}/comments?extended=full")
  suspend fun fetchShowComments(
    @Path("traktId") traktId: Long,
    @Query("limit") limit: Int
  ): List<Comment>

  @GET("movies/{traktId}/comments?extended=full")
  suspend fun fetchMovieComments(
    @Path("traktId") traktId: Long,
    @Query("limit") limit: Int
  ): List<Comment>

  @GET("shows/{traktId}/translations/{code}")
  suspend fun fetchShowTranslations(
    @Path("traktId") traktId: Long,
    @Path("code") countryCode: String,
  ): List<Translation>

  @GET("movies/{traktId}/translations/{code}")
  suspend fun fetchMovieTranslations(
    @Path("traktId") traktId: Long,
    @Path("code") countryCode: String,
  ): List<Translation>

  @GET("shows/{showId}/seasons/{seasonNumber}")
  suspend fun fetchSeasonTranslations(
    @Path("showId") showTraktId: Long,
    @Path("seasonNumber") seasonNumber: Int,
    @Query("translations") countryCode: String,
  ): List<SeasonTranslation>

  @GET("shows/{traktId}/seasons/{seasonNumber}/episodes/{episodeNumber}/comments?limit=40&extended=full")
  suspend fun fetchEpisodeComments(
    @Path("traktId") traktId: Long,
    @Path("seasonNumber") seasonNumber: Int,
    @Path("episodeNumber") episodeNumber: Int
  ): List<Comment>

  @GET("shows/{traktId}/people")
  suspend fun fetchShowPeople(
    @Path("traktId") traktId: Long
  ): ActorsResponse

  // Auth

  @POST("oauth/token")
  suspend fun fetchOAuthToken(@Body request: OAuthRequest): OAuthResponse

  @POST("oauth/token")
  suspend fun refreshOAuthToken(@Body request: OAuthRefreshRequest): OAuthResponse

  @POST("oauth/revoke")
  suspend fun revokeOAuthToken(@Body request: OAuthRevokeRequest): Response<Any>

  @GET("users/me")
  suspend fun fetchMyProfile(@Header("Authorization") authToken: String): User

  // Sync

  @GET("users/hidden/progress_watched?type=show")
  suspend fun fetchHiddenShows(
    @Header("Authorization") authToken: String,
    @Query("limit") pageLimit: Int
  ): List<HiddenItem>

  @GET("sync/watched/shows")
  suspend fun fetchSyncWatched(
    @Header("Authorization") authToken: String,
    @Query("extended") extended: String?
  ): List<SyncItem>

  @GET("sync/watchlist?extended=full")
  suspend fun fetchSyncWatchlist(@Header("Authorization") authToken: String): List<SyncItem>

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

  @GET("sync/ratings/episodes")
  suspend fun fetchEpisodesRatings(
    @Header("Authorization") authToken: String
  ): List<RatingResultEpisode>
}
