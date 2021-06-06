package com.michaldrabik.ui_settings.cases

import com.michaldrabik.repository.movies.MovieStreamingsRepository
import com.michaldrabik.repository.shows.ShowStreamingsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SettingsStreamingsCase @Inject constructor(
  private val showStreamingsRepository: ShowStreamingsRepository,
  private val movieStreamingsRepository: MovieStreamingsRepository,
) {

  suspend fun deleteCache() {
    showStreamingsRepository.deleteCache()
    movieStreamingsRepository.deleteCache()
  }
}
