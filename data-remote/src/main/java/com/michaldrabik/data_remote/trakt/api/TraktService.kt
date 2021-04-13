package com.michaldrabik.data_remote.trakt.api

import com.michaldrabik.data_remote.Config
import com.michaldrabik.data_remote.trakt.model.ActorsResponse
import com.michaldrabik.data_remote.trakt.model.Comment
import com.michaldrabik.data_remote.trakt.model.CustomList
import com.michaldrabik.data_remote.trakt.model.Episode
import com.michaldrabik.data_remote.trakt.model.HiddenItem
import com.michaldrabik.data_remote.trakt.model.Movie
import com.michaldrabik.data_remote.trakt.model.MovieResult
import com.michaldrabik.data_remote.trakt.model.OAuthResponse
import com.michaldrabik.data_remote.trakt.model.RatingResultEpisode
import com.michaldrabik.data_remote.trakt.model.RatingResultMovie
import com.michaldrabik.data_remote.trakt.model.RatingResultShow
import com.michaldrabik.data_remote.trakt.model.SearchResult
import com.michaldrabik.data_remote.trakt.model.Season
import com.michaldrabik.data_remote.trakt.model.SeasonTranslation
import com.michaldrabik.data_remote.trakt.model.Show
import com.michaldrabik.data_remote.trakt.model.ShowResult
import com.michaldrabik.data_remote.trakt.model.SyncExportRequest
import com.michaldrabik.data_remote.trakt.model.SyncExportResult
import com.michaldrabik.data_remote.trakt.model.SyncItem
import com.michaldrabik.data_remote.trakt.model.Translation
import com.michaldrabik.data_remote.trakt.model.User
import com.michaldrabik.data_remote.trakt.model.request.CommentRequest
import com.michaldrabik.data_remote.trakt.model.request.CreateListRequest
import com.michaldrabik.data_remote.trakt.model.request.OAuthRefreshRequest
import com.michaldrabik.data_remote.trakt.model.request.OAuthRequest
import com.michaldrabik.data_remote.trakt.model.request.OAuthRevokeRequest
import com.michaldrabik.data_remote.trakt.model.request.RatingRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
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

  @GET("shows/trending?extended=full")
  suspend fun fetchTrendingShows(
    @Query("genres") genres: String,
    @Query("limit") limit: Int
  ): List<ShowResult>

  @GET("movies/trending?extended=full")
  suspend fun fetchTrendingMovies(
    @Query("genres") genres: String,
    @Query("limit") limit: Int
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

  @GET("movies/{traktId}/related?extended=full&limit=${Config.TRAKT_RELATED_MOVIES_LIMIT}")
  suspend fun fetchRelatedMovies(@Path("traktId") traktId: Long): List<Movie>

  @GET("shows/{traktId}/next_episode?extended=full")
  suspend fun fetchNextEpisode(@Path("traktId") traktId: Long): Response<Episode>

  @GET("search/show?extended=full&limit=${Config.TRAKT_SEARCH_LIMIT}")
  suspend fun fetchSearchResults(@Query("query") queryText: String): List<SearchResult>

  @GET("search/show,movie?extended=full&limit=${Config.TRAKT_SEARCH_LIMIT}")
  suspend fun fetchSearchResultsMovies(@Query("query") queryText: String): List<SearchResult>

  @GET("shows/{traktId}/seasons?extended=full,episodes")
  suspend fun fetchSeasons(@Path("traktId") traktId: Long): List<Season>

  @GET("shows/{traktId}/comments/newest?extended=full")
  suspend fun fetchShowComments(
    @Path("traktId") traktId: Long,
    @Query("limit") limit: Int,
    @Query("timestamp") timestamp: Long
  ): List<Comment>

  @GET("movies/{traktId}/comments/newest?extended=full")
  suspend fun fetchMovieComments(
    @Path("traktId") traktId: Long,
    @Query("limit") limit: Int,
    @Query("timestamp") timestamp: Long
  ): List<Comment>

  @GET("comments/{id}/replies")
  suspend fun fetchCommentReplies(
    @Path("id") commentId: Long,
    @Query("timestamp") timestamp: Long
  ): List<Comment>

  @POST("comments")
  suspend fun postComment(
    @Header("Authorization") authToken: String,
    @Body commentBody: CommentRequest
  ): Comment

  @POST("comments/{id}/replies")
  suspend fun postCommentReply(
    @Header("Authorization") authToken: String,
    @Path("id") commentId: Long,
    @Body commentBody: CommentRequest
  ): Comment

  @DELETE("comments/{id}")
  suspend fun deleteComment(
    @Header("Authorization") authToken: String,
    @Path("id") commentIt: Long
  ): Response<Any>

  @GET("shows/{traktId}/translations/{code}")
  suspend fun fetchShowTranslations(
    @Path("traktId") traktId: Long,
    @Path("code") countryCode: String
  ): List<Translation>

  @GET("movies/{traktId}/translations/{code}")
  suspend fun fetchMovieTranslations(
    @Path("traktId") traktId: Long,
    @Path("code") countryCode: String
  ): List<Translation>

  @GET("shows/{showId}/seasons/{seasonNumber}")
  suspend fun fetchSeasonTranslations(
    @Path("showId") showTraktId: Long,
    @Path("seasonNumber") seasonNumber: Int,
    @Query("translations") countryCode: String
  ): List<SeasonTranslation>

  @GET("shows/{traktId}/seasons/{seasonNumber}/episodes/{episodeNumber}/comments/newest?limit=50&extended=full")
  suspend fun fetchEpisodeComments(
    @Path("traktId") traktId: Long,
    @Path("seasonNumber") seasonNumber: Int,
    @Path("episodeNumber") episodeNumber: Int,
    @Query("timestamp") timestamp: Long
  ): List<Comment>

  @GET("shows/{traktId}/people")
  suspend fun fetchShowPeople(
    @Path("traktId") traktId: Long
  ): ActorsResponse

  @GET("movies/{traktId}/people")
  suspend fun fetchMoviePeople(
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

  @GET("users/hidden/progress_watched?type=movie")
  suspend fun fetchHiddenMovies(
    @Header("Authorization") authToken: String,
    @Query("limit") pageLimit: Int
  ): List<HiddenItem>

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

  @GET("users/me/lists")
  suspend fun fetchSyncLists(
    @Header("Authorization") authToken: String,
  ): List<CustomList>

  @GET("users/me/lists/{id}")
  suspend fun fetchSyncList(
    @Header("Authorization") authToken: String,
    @Path("id") listId: Long
  ): CustomList

  @GET("users/me/lists/{id}/items/{types}?extended=full")
  suspend fun fetchSyncListItems(
    @Header("Authorization") authToken: String,
    @Path("id") listId: Long,
    @Path("types") types: String,
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

  @POST("users/me/lists")
  suspend fun postCreateList(
    @Header("Authorization") authToken: String,
    @Body request: CreateListRequest
  ): CustomList

  @PUT("users/me/lists/{id}")
  suspend fun postUpdateList(
    @Header("Authorization") authToken: String,
    @Path("id") listId: Long,
    @Body request: CreateListRequest
  ): CustomList

  @DELETE("users/me/lists/{id}")
  suspend fun deleteList(
    @Header("Authorization") authToken: String,
    @Path("id") listId: Long
  ): Response<Any>

  @POST("users/me/lists/{id}/items")
  suspend fun postAddListItems(
    @Header("Authorization") authToken: String,
    @Path("id") listId: Long,
    @Body request: SyncExportRequest
  ): SyncExportResult

  @POST("users/me/lists/{id}/items/remove")
  suspend fun postRemoveListItems(
    @Header("Authorization") authToken: String,
    @Path("id") listId: Long,
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
}
