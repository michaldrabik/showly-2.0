package com.michaldrabik.data_remote.tmdb.api

import com.michaldrabik.data_remote.tmdb.model.TmdbImages
import com.michaldrabik.data_remote.tmdb.model.TmdbPerson
import com.michaldrabik.data_remote.tmdb.model.TmdbStreamingCountry
import com.michaldrabik.data_remote.tmdb.model.TmdbTranslation
import java.util.Locale.ROOT

class TmdbApi(private val service: TmdbService) {

  suspend fun fetchShowImages(tmdbId: Long) =
    try {
      if (tmdbId <= 0) TmdbImages.EMPTY
      service.fetchShowImages(tmdbId)
    } catch (error: Throwable) {
      TmdbImages.EMPTY
    }

  suspend fun fetchEpisodeImage(showTmdbId: Long?, season: Int?, episode: Int?) =
    try {
      if (showTmdbId == null || showTmdbId <= 0) TmdbImages.EMPTY
      if (season == null || season <= 0) TmdbImages.EMPTY
      if (episode == null || episode <= 0) TmdbImages.EMPTY
      val images = service.fetchEpisodeImages(showTmdbId, season, episode)
      images.stills?.firstOrNull()
    } catch (error: Throwable) {
      null
    }

  suspend fun fetchMovieImages(tmdbId: Long) =
    try {
      if (tmdbId <= 0) TmdbImages.EMPTY
      service.fetchMovieImages(tmdbId)
    } catch (error: Throwable) {
      TmdbImages.EMPTY
    }

  suspend fun fetchMoviePeople(tmdbId: Long): Map<TmdbPerson.Type, List<TmdbPerson>> {
    val result = service.fetchMoviePeople(tmdbId)
    val cast = result.cast?.toList() ?: emptyList()
    val crew = result.crew?.toList() ?: emptyList()
    return mapOf(
      TmdbPerson.Type.CAST to cast,
      TmdbPerson.Type.CREW to crew
    )
  }

  suspend fun fetchShowPeople(tmdbId: Long): Map<TmdbPerson.Type, List<TmdbPerson>> {
    val result = service.fetchShowPeople(tmdbId)
    val cast = result.cast?.toList() ?: emptyList()
    val crew = result.crew?.toList() ?: emptyList()
    return mapOf(
      TmdbPerson.Type.CAST to cast,
      TmdbPerson.Type.CREW to crew
    )
  }

  suspend fun fetchShowWatchProviders(tmdbId: Long, countryCode: String): TmdbStreamingCountry? {
    val result = service.fetchShowWatchProviders(tmdbId)
    return result.results[countryCode.uppercase(ROOT)]
  }

  suspend fun fetchMovieWatchProviders(tmdbId: Long, countryCode: String): TmdbStreamingCountry? {
    val result = service.fetchMovieWatchProviders(tmdbId)
    return result.results[countryCode.uppercase(ROOT)]
  }

  suspend fun fetchPersonDetails(id: Long): TmdbPerson {
    return service.fetchPersonDetails(id)
  }

  suspend fun fetchPersonTranslations(id: Long): Map<String, TmdbTranslation.Data> {
    val result = service.fetchPersonTranslation(id).translations ?: emptyList()
    return result.associateBy({ it.iso_639_1.lowercase() }, { it.data ?: TmdbTranslation.Data(null) })
  }

  suspend fun fetchPersonImages(tmdbId: Long) =
    try {
      if (tmdbId <= 0) TmdbImages.EMPTY
      service.fetchPersonImages(tmdbId)
    } catch (error: Throwable) {
      TmdbImages.EMPTY
    }
}
