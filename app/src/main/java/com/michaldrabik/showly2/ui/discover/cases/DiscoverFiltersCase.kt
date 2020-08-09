package com.michaldrabik.showly2.ui.discover.cases

import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.DiscoverFilters
import com.michaldrabik.showly2.repository.settings.SettingsRepository
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
        showAnticipatedShows = !filters.hideAnticipated
      )
    )
  }

  suspend fun loadFilters(): DiscoverFilters {
    val settings = settingsRepository.load()
    return DiscoverFilters(
      feedOrder = settings.discoverFilterFeed,
      hideAnticipated = !settings.showAnticipatedShows,
      genres = settings.discoverFilterGenres.toList()
    )
  }

}
