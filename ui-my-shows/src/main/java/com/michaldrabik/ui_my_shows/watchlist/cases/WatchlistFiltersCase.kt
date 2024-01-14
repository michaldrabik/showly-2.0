package com.michaldrabik.ui_my_shows.watchlist.cases

import com.michaldrabik.repository.settings.SettingsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class WatchlistFiltersCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun setIsUpcoming(isUpcoming: Boolean) {
    settingsRepository.filters.watchlistShowsUpcoming = isUpcoming
  }
}
