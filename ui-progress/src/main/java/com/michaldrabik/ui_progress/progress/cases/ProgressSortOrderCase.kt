package com.michaldrabik.ui_progress.progress.cases

import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_model.SortOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressSortOrderCase @Inject constructor(
  private val settingsRepository: SettingsRepository
) {

  suspend fun setSortOrder(sortOrder: SortOrder) {
    val settings = settingsRepository.load()
    settingsRepository.update(settings.copy(progressSortOrder = sortOrder))
  }

  suspend fun loadSortOrder(): SortOrder {
    if (!settingsRepository.isInitialized()) {
      return Settings.createInitial().progressSortOrder
    }
    return settingsRepository.load().progressSortOrder
  }
}
