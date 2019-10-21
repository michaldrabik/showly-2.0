package com.michaldrabik.network.trakt.api

import com.michaldrabik.network.trakt.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TraktService {

  @GET("shows/{traktId}?extended=full")
  suspend fun fetchShow(@Path("traktId") traktId: Long): Show

  @GET("shows/trending?extended=full&limit=252")
  suspend fun fetchTrendingShows(): List<ShowResult>

  @GET("shows/anticipated?extended=full&limit=30")
  suspend fun fetchAnticipatedShows(): List<ShowResult>

  @GET("shows/{traktId}/related?extended=full&limit=15")
  suspend fun fetchRelatedShows(@Path("traktId") traktId: Long): List<Show>

  @GET("shows/{traktId}/next_episode?extended=full")
  suspend fun fetchNextEpisode(@Path("traktId") traktId: Long): Response<Episode>

  @GET("search/show?extended=full&limit=50")
  suspend fun fetchSearchResults(@Query("query") queryText: String): List<SearchResult>

  @GET("shows/{traktId}/seasons?extended=full,episodes")
  suspend fun fetchSeasons(@Path("traktId") traktId: Long): List<Season>
}