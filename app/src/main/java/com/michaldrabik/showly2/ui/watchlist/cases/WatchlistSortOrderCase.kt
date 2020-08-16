package com.michaldrabik.showly2.ui.watchlist.cases

import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Settings
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.repository.settings.SettingsRepository
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
