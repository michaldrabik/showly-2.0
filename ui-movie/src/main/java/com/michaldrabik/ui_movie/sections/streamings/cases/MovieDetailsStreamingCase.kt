package com.michaldrabik.ui_movie.sections.streamings.cases

import com.michaldrabik.common.ConfigVariant
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.repository.movies.MovieStreamingsRepository
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.StreamingService
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsStreamingCase @Inject constructor(
  private val streamingsRepository: MovieStreamingsRepository,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun getLocalStreamingServices(movie: Movie): List<StreamingService> {
    if (!settingsRepository.streamingsEnabled) {
      return emptyList()
    }
    val country = AppCountry.fromCode(settingsRepository.country)
    val localData = streamingsRepository.getLocalStreamings(movie, country.code)
    return localData.first
  }

  suspend fun loadStreamingServices(movie: Movie): List<StreamingService> {
    if (!settingsRepository.streamingsEnabled) {
      return emptyList()
    }
    val country = AppCountry.fromCode(settingsRepository.country)
    val (localItems, timestamp) = streamingsRepository.getLocalStreamings(movie, country.code)
    if (timestamp != null && timestamp.plusSeconds(ConfigVariant.STREAMINGS_CACHE_DURATION / 1000).isAfter(nowUtc())) {
      return localItems
    }
    return streamingsRepository.loadRemoteStreamings(movie, country.code)
  }
}
