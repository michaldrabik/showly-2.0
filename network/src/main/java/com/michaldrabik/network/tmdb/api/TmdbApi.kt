package com.michaldrabik.network.tmdb.api

import com.michaldrabik.network.tmdb.model.TmdbActor

class TmdbApi(private val service: TmdbService) {

  suspend fun fetchMovieImages(tmdbId: Long) = service.fetchImages(tmdbId)

  suspend fun fetchMovieActors(tmdbId: Long): List<TmdbActor> {
    val result = service.fetchActors(tmdbId)
    return result.cast?.map { it.copy(movieTmdbId = result.id) } ?: emptyList()
  }
}
