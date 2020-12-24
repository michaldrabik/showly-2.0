package com.michaldrabik.ui_discover_movies.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_repository.SettingsRepository
import javax.inject.Inject

@AppScope
class DiscoverFiltersCase @Inject constructor(
  private val settingsRepository: SettingsRepository
) {

  suspend fun saveFilters(filters: DiscoverFilters) {
    val settings = settingsRepository.load()
    settingsRepository.update(
      settings.copy(
        discoverMoviesFilterFeed = filters.feedOrder,
        discoverMoviesFilterGenres = filters.genres,
        showAnticipatedMovies = !filters.hideAnticipated,
        showCollectionMovies = !filters.hideCollection
      )
    )
    Analytics.logDiscoverMoviesFiltersApply(filters)
  }

  suspend fun loadFilters(): DiscoverFilters {
    val settings = settingsRepository.load()
    return DiscoverFilters(
      feedOrder = settings.discoverMoviesFilterFeed,
      hideAnticipated = !settings.showAnticipatedMovies,
      hideCollection = !settings.showCollectionMovies,
      genres = settings.discoverMoviesFilterGenres.toList()
    )
  }
}
