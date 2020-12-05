package com.michaldrabik.ui_progress.main.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_repository.SettingsRepository
import javax.inject.Inject

@AppScope
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
