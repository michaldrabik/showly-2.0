package com.michaldrabik.ui_my_movies.hidden.cases

import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class HiddenSortOrderCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun setSortOrder(sortOrder: SortOrder, sortType: SortType) {
    settingsRepository.sortSettings.hiddenMoviesSortOrder = sortOrder
    settingsRepository.sortSettings.hiddenMoviesSortType = sortType
  }

  fun loadSortOrder() = Pair(
    settingsRepository.sortSettings.hiddenMoviesSortOrder,
    settingsRepository.sortSettings.hiddenMoviesSortType
  )
}
