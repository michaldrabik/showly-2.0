package com.michaldrabik.ui_discover.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.DiscoverFilters
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class DiscoverFiltersCase @Inject constructor(
  private val settingsRepository: SettingsRepository
) {

  suspend fun loadFilters(): DiscoverFilters {
    val settings = settingsRepository.load()
    return DiscoverFilters(
      feedOrder = settings.discoverFilterFeed,
      hideAnticipated = !settings.showAnticipatedShows,
      hideCollection = !settings.showCollectionShows,
      genres = settings.discoverFilterGenres.toList(),
      networks = settings.discoverFilterNetworks.toList()
    )
  }
}
