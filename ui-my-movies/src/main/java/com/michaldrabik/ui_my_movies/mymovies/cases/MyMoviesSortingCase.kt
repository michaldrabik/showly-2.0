package com.michaldrabik.ui_my_movies.mymovies.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MyMoviesSortingCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun loadSortOrder() = Pair(
    settingsRepository.sorting.myMoviesAllSortOrder,
    settingsRepository.sorting.myMoviesAllSortType
  )

  fun setSortOrder(sortOrder: SortOrder, sortType: SortType) {
    settingsRepository.sorting.myMoviesAllSortOrder = sortOrder
    settingsRepository.sorting.myMoviesAllSortType = sortType
  }
}
