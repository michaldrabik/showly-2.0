package com.michaldrabik.ui_progress.progress.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ProgressSortOrderCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun setSortOrder(sortOrder: SortOrder, sortType: SortType, newAtTop: Boolean) {
    settingsRepository.sorting.progressShowsSortOrder = sortOrder
    settingsRepository.sorting.progressShowsSortType = sortType
    settingsRepository.sorting.progressShowsNewAtTop = newAtTop
  }

  fun loadSortOrder() = Triple(
    settingsRepository.sorting.progressShowsSortOrder,
    settingsRepository.sorting.progressShowsSortType,
    settingsRepository.sorting.progressShowsNewAtTop
  )
}
