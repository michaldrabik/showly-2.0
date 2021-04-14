package com.michaldrabik.data_remote.reddit.api

import com.michaldrabik.data_remote.reddit.model.RedditResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RedditService {

  @GET("r/television/hot/.json")
  suspend fun fetchTelevision(
    @Query("limit") limit: Int,
  ): RedditResponse

  @GET("r/movies/hot/.json")
  suspend fun fetchMovies(
    @Query("limit") limit: Int,
  ): RedditResponse
}
