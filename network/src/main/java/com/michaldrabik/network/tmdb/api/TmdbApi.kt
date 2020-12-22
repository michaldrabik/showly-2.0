package com.michaldrabik.network.tmdb.api

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.michaldrabik.network.tmdb.model.TmdbActor
import com.michaldrabik.network.tmdb.model.TmdbImages
import kotlin.coroutines.cancellation.CancellationException

class TmdbApi(private val service: TmdbService) {

  suspend fun fetchMovieImages(tmdbId: Long) =
    try {
      if (tmdbId <= 0) TmdbImages(emptyList(), emptyList())
      service.fetchImages(tmdbId)
    } catch (error: Throwable) {
      if (error !is CancellationException) {
        FirebaseCrashlytics.getInstance().run {
          setCustomKey("Source", "${TmdbApi::class.simpleName}::fetchMovieImages()")
          recordException(error)
        }
      }
      TmdbImages(emptyList(), emptyList())
    }

  suspend fun fetchMovieActors(tmdbId: Long): List<TmdbActor> {
    val result = service.fetchActors(tmdbId)
    return result.cast?.map { it.copy(movieTmdbId = result.id) } ?: emptyList()
  }
}
