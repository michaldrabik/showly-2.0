package com.michaldrabik.network.tmdb.api

import com.michaldrabik.network.tmdb.model.TmdbActors
import com.michaldrabik.network.tmdb.model.TmdbImages
import retrofit2.http.GET
import retrofit2.http.Path

interface TmdbService {

  @GET("movie/{tmdbId}/images")
  suspend fun fetchImages(@Path("tmdbId") tmdbId: Long): TmdbImages

  @GET("movie/{tmdbId}/credits")
  suspend fun fetchActors(@Path("tmdbId") tmdbId: Long): TmdbActors
}
