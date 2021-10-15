package com.michaldrabik.ui_my_movies.hidden.cases

import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_model.SortOrder
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class HiddenSortOrderCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun setSortOrder(sortOrder: SortOrder) {
    settingsRepository.hiddenMoviesSortOrder = sortOrder
  }

  fun loadSortOrder() =
    settingsRepository.hiddenMoviesSortOrder
}
