package com.michaldrabik.ui_progress_movies.progress.cases

import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_model.SortOrder
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ProgressMoviesSortCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  suspend fun setSortOrder(sortOrder: SortOrder) {
    val settings = settingsRepository.load()
    settingsRepository.update(settings.copy(progressMoviesSortBy = sortOrder))
  }

  suspend fun loadSortOrder(): SortOrder {
    if (!settingsRepository.isInitialized()) {
      return Settings.createInitial().progressMoviesSortBy
    }
    return settingsRepository.load().progressMoviesSortBy
  }
}
