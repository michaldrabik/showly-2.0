package com.michaldrabik.ui_search.cases

import com.michaldrabik.common.Mode.MOVIES
import com.michaldrabik.common.Mode.SHOWS
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_search.recycler.SearchListItem
import com.michaldrabik.ui_search.utilities.SearchOptions
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SearchFiltersCase @Inject constructor(
  private val settingsRepository: SettingsRepository
) {

  val isMoviesEnabled by lazy { settingsRepository.isMoviesEnabled }

  fun filter(
    searchOptions: SearchOptions,
    item: SearchListItem
  ): Boolean {
    val filterNone = searchOptions.filters.isEmpty() || searchOptions.filters.containsAll(listOf(SHOWS, MOVIES))
    return when {
      filterNone -> true
      searchOptions.filters.contains(SHOWS) -> item.isShow
      searchOptions.filters.contains(MOVIES) && isMoviesEnabled -> item.isMovie
      searchOptions.filters.contains(MOVIES) && !isMoviesEnabled -> false
      else -> true
    }
  }
}
