package com.michaldrabik.ui_my_movies.mymovies.cases

import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MyMoviesSortingCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun loadSortOrder() = Pair(
    settingsRepository.sortSettings.myMoviesAllSortOrder,
    settingsRepository.sortSettings.myMoviesAllSortType
  )

  fun setSortOrder(sortOrder: SortOrder, sortType: SortType) {
    settingsRepository.sortSettings.myMoviesAllSortOrder = sortOrder
    settingsRepository.sortSettings.myMoviesAllSortType = sortType
  }
}
