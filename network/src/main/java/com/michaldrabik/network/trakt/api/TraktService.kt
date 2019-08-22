package com.michaldrabik.network.trakt.api

import com.michaldrabik.network.trakt.model.TrendingResult
import retrofit2.http.GET

interface TraktService {

  @GET("shows/trending?extended=full,images")
  suspend fun fetchTrendingShows(): List<TrendingResult>

}