package com.michaldrabik.ui_watchlist.main.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_repository.SettingsRepository
import javax.inject.Inject

@AppScope
class WatchlistSortOrderCase @Inject constructor(
  private val settingsRepository: SettingsRepository
) {

  suspend fun setSortOrder(sortOrder: SortOrder) {
    val settings = settingsRepository.load()
    settingsRepository.update(settings.copy(watchlistSortOrder = sortOrder))
  }

  suspend fun loadSortOrder(): SortOrder {
    if (!settingsRepository.isInitialized()) {
      return Settings.createInitial().watchlistSortOrder
    }
    return settingsRepository.load().watchlistSortOrder
  }
}
