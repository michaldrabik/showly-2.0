package com.michaldrabik.ui_discover.cases

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
        discoverFilterFeed = filters.feedOrder,
        discoverFilterGenres = filters.genres,
        showAnticipatedShows = !filters.hideAnticipated,
        showCollectionShows = !filters.hideCollection
      )
    )
    Analytics.logDiscoverFiltersApply(filters)
  }

  suspend fun loadFilters(): DiscoverFilters {
    val settings = settingsRepository.load()
    return DiscoverFilters(
      feedOrder = settings.discoverFilterFeed,
      hideAnticipated = !settings.showAnticipatedShows,
      hideCollection = !settings.showCollectionShows,
      genres = settings.discoverFilterGenres.toList()
    )
  }
}
