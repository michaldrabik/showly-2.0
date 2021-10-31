package com.michaldrabik.ui_my_shows.watchlist.cases

import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class WatchlistSortOrderCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun setSortOrder(sortOrder: SortOrder, sortType: SortType) {
    settingsRepository.sortSettings.watchlistShowsSortOrder = sortOrder
    settingsRepository.sortSettings.watchlistShowsSortType = sortType
  }

  fun loadSortOrder() = Pair(
    settingsRepository.sortSettings.watchlistShowsSortOrder,
    settingsRepository.sortSettings.watchlistShowsSortType
  )
}
