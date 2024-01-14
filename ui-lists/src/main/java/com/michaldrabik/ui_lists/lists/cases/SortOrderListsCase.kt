package com.michaldrabik.ui_lists.lists.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SortOrderListsCase @Inject constructor(
  private val settingsRepository: SettingsRepository
) {

  fun setSortOrder(sortOrder: SortOrder, sortType: SortType) {
    settingsRepository.sorting.listsAllSortOrder = sortOrder
    settingsRepository.sorting.listsAllSortType = sortType
  }

  fun loadSortOrder() = Pair(
    settingsRepository.sorting.listsAllSortOrder,
    settingsRepository.sorting.listsAllSortType
  )
}
