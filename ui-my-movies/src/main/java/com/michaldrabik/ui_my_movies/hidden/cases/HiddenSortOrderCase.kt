package com.michaldrabik.ui_my_movies.hidden.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class HiddenSortOrderCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun setSortOrder(sortOrder: SortOrder, sortType: SortType) {
    settingsRepository.sorting.hiddenMoviesSortOrder = sortOrder
    settingsRepository.sorting.hiddenMoviesSortType = sortType
  }

  fun loadSortOrder() = Pair(
    settingsRepository.sorting.hiddenMoviesSortOrder,
    settingsRepository.sorting.hiddenMoviesSortType
  )
}
