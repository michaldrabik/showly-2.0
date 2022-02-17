package com.michaldrabik.ui_my_shows.archive.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ArchiveSortOrderCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {

  fun setSortOrder(sortOrder: SortOrder, sortType: SortType) {
    settingsRepository.sortSettings.hiddenShowsSortOrder = sortOrder
    settingsRepository.sortSettings.hiddenShowsSortType = sortType
  }

  fun loadSortOrder() = Pair(
    settingsRepository.sortSettings.hiddenShowsSortOrder,
    settingsRepository.sortSettings.hiddenShowsSortType
  )
}
