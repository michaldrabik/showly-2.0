package com.michaldrabik.ui_my_shows.watchlist.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_model.SortOrder
import javax.inject.Inject

@AppScope
class WatchlistSortOrderCase @Inject constructor(
  private val settingsRepository: SettingsRepository
) {

  suspend fun setSortOrder(sortOrder: SortOrder) {
    val settings = settingsRepository.load()
    settingsRepository.update(settings.copy(watchlistShowsSortBy = sortOrder))
  }

  suspend fun loadSortOrder() =
    settingsRepository.load().watchlistShowsSortBy
}
