package com.michaldrabik.ui_discover_movies.cases

import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_model.DiscoverFilters
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
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
