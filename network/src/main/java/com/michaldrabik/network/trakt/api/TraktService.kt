package com.michaldrabik.network.trakt.api

import com.michaldrabik.network.trakt.model.TrendingResult
import retrofit2.http.GET

interface TraktService {

  @GET("shows/trending?extended=full&limit=154")
  suspend fun fetchTrendingShows(): List<TrendingResult>

}