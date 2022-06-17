package com.michaldrabik.data_remote.trakt.api.service

import com.michaldrabik.data_remote.trakt.model.Comment
import com.michaldrabik.data_remote.trakt.model.Episode
import com.michaldrabik.data_remote.trakt.model.Season
import com.michaldrabik.data_remote.trakt.model.SeasonTranslation
import com.michaldrabik.data_remote.trakt.model.Show
import com.michaldrabik.data_remote.trakt.model.ShowResult
import com.michaldrabik.data_remote.trakt.model.Translation
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TraktShowsService {

  @GET("shows/{traktId}?extended=full")
  suspend fun fetchShow(@Path("traktId") traktId: Long): Show

  @GET("shows/{traktSlug}?extended=full")
  suspend fun fetchShow(@Path("traktSlug") traktSlug: String): Show

  @GET("shows/popular?extended=full")
  suspend fun fetchPopularShows(
    @Query("genres") genres: String,
    @Query("networks") networks: String,
    @Query("limit") limit: Int
  ): List<Show>

  @GET("shows/trending?extended=full")
  suspend fun fetchTrendingShows(
    @Query("genres") genres: String,
    @Query("networks") networks: String,
    @Query("limit") limit: Int
  ): List<ShowResult>

  @GET("shows/anticipated?extended=full")
  suspend fun fetchAnticipatedShows(
    @Query("genres") genres: String,
    @Query("networks") networks: String,
    @Query("limit") limit: Int
  ): List<ShowResult>

  @GET("shows/{traktId}/related?extended=full")
  suspend fun fetchRelatedShows(@Path("traktId") traktId: Long, @Query("limit") limit: Int): List<Show>

  @GET("shows/{traktId}/next_episode?extended=full")
  suspend fun fetchNextEpisode(@Path("traktId") traktId: Long): Response<Episode>

  @GET("shows/{traktId}/seasons?extended=full,episodes")
  suspend fun fetchSeasons(@Path("traktId") traktId: Long): List<Season>

  @GET("shows/{traktId}/comments/newest?extended=full")
  suspend fun fetchShowComments(
    @Path("traktId") traktId: Long,
    @Query("limit") limit: Int,
    @Query("timestamp") timestamp: Long
  ): List<Comment>

  @GET("shows/{traktId}/translations/{code}")
  suspend fun fetchShowTranslations(
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
}
