package com.michaldrabik.network.tmdb.api

import com.michaldrabik.network.tmdb.model.TmdbActor
import com.michaldrabik.network.tmdb.model.TmdbImages

class TmdbApi(private val service: TmdbService) {

  suspend fun fetchMovieImages(tmdbId: Long) =
    try {
      if (tmdbId <= 0) TmdbImages(emptyList(), emptyList())
      service.fetchImages(tmdbId)
    } catch (error: Throwable) {
      TmdbImages(emptyList(), emptyList())
    }

  suspend fun fetchMovieActors(tmdbId: Long): List<TmdbActor> {
    val result = service.fetchActors(tmdbId)
    return result.cast?.map { it.copy(movieTmdbId = result.id) } ?: emptyList()
  }
}
