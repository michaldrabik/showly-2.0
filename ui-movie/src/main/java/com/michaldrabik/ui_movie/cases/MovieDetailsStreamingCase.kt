package com.michaldrabik.ui_movie.cases

import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.StreamingsRepository
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.StreamingService
import javax.inject.Inject

class MovieDetailsStreamingCase @Inject constructor(
  private val streamingsRepository: StreamingsRepository,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun loadStreamingServices(movie: Movie): List<StreamingService> {
    val country = AppCountry.fromCode(settingsRepository.country)
    return streamingsRepository.loadStreamings(movie, country.code)
  }
}
