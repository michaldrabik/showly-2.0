package com.michaldrabik.network.trakt.api

import com.michaldrabik.network.trakt.model.Episode
import com.michaldrabik.network.trakt.model.SearchResult
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.network.trakt.model.TrendingResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TraktService {

  @GET("shows/{traktId}?extended=full")
  suspend fun fetchShow(@Path("traktId") traktId: Long): Show

  @GET("shows/trending?extended=full&limit=154")
  suspend fun fetchTrendingShows(): List<TrendingResult>

  @GET("shows/{traktId}/related?extended=full")
  suspend fun fetchRelatedShows(@Path("traktId") traktId: Long): List<Show>

  @GET("shows/{traktId}/next_episode?extended=full")
  suspend fun fetchNextEpisode(@Path("traktId") traktId: Long): Response<Episode>

  @GET("search/show?extended=full&limit=50")
  suspend fun fetchSearchResults(@Query("query") queryText: String): List<SearchResult>
}