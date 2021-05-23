package com.michaldrabik.data_remote.tmdb.api

import com.michaldrabik.data_remote.tmdb.model.TmdbActors
import com.michaldrabik.data_remote.tmdb.model.TmdbImages
import com.michaldrabik.data_remote.tmdb.model.TmdbStreamings
import retrofit2.http.GET
import retrofit2.http.Path

interface TmdbService {

  @GET("tv/{tmdbId}/images")
  suspend fun fetchShowImages(@Path("tmdbId") tmdbId: Long): TmdbImages

  @GET("tv/{tmdbId}/season/{season}/episode/{episode}/images")
  suspend fun fetchEpisodeImages(
    @Path("tmdbId") tmdbId: Long?,
    @Path("season") seasonNumber: Int?,
    @Path("episode") episodeNumber: Int?
  ): TmdbImages

  @GET("movie/{tmdbId}/images")
  suspend fun fetchMovieImages(@Path("tmdbId") tmdbId: Long): TmdbImages

  @GET("movie/{tmdbId}/credits")
  suspend fun fetchMovieActors(@Path("tmdbId") tmdbId: Long): TmdbActors

  @GET("tv/{tmdbId}/credits")
  suspend fun fetchShowActors(@Path("tmdbId") tmdbId: Long): TmdbActors

  @GET("movie/{tmdbId}/watch/providers")
  suspend fun fetchMovieWatchProviders(@Path("tmdbId") tmdbId: Long): TmdbStreamings

  @GET("tv/{tmdbId}/watch/providers")
  suspend fun fetchShowWatchProviders(@Path("tmdbId") tmdbId: Long): TmdbStreamings
}
