package com.michaldrabik.network.tmdb.api

class TmdbApi(private val service: TmdbService) {

  suspend fun fetchMovieImages(tmdbId: Long) = service.fetchImages(tmdbId)
}
