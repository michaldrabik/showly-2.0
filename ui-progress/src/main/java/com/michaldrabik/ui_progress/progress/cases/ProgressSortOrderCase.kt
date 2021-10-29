package com.michaldrabik.ui_progress.progress.cases

import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ProgressSortOrderCase @Inject constructor(
  private val settingsRepository: SettingsRepository
) {

  fun setSortOrder(sortOrder: SortOrder, sortType: SortType) {
    settingsRepository.sortSettings.progressShowsSortOrder = sortOrder
    settingsRepository.sortSettings.progressShowsSortType = sortType
  }

  fun loadSortOrder() = Pair(
    settingsRepository.sortSettings.progressShowsSortOrder,
    settingsRepository.sortSettings.progressShowsSortType
  )
}
