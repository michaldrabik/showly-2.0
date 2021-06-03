package com.michaldrabik.ui_settings.cases

import com.michaldrabik.repository.movies.MovieStreamingsRepository
import com.michaldrabik.repository.shows.ShowStreamingsRepository
import javax.inject.Inject

class SettingsStreamingsCase @Inject constructor(
  private val showStreamingsRepository: ShowStreamingsRepository,
  private val movieStreamingsRepository: MovieStreamingsRepository,
) {

  suspend fun deleteCache() {
    showStreamingsRepository.deleteCache()
    movieStreamingsRepository.deleteCache()
  }
}
