package com.michaldrabik.network.trakt.api

import com.michaldrabik.network.trakt.model.Episode
import com.michaldrabik.network.trakt.model.TrendingResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TraktService {

  @GET("shows/trending?extended=full&limit=154")
  suspend fun fetchTrendingShows(): List<TrendingResult>

  @GET("shows/{traktId}/next_episode?extended=full")
  suspend fun fetchNextEpisode(@Path("traktId") traktId: Long): Response<Episode>
}