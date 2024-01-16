package com.michaldrabik.ui_my_shows.watchlist.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.UpcomingFilter
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class WatchlistFiltersCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun toggleUpcomingFilter() {
    val current = settingsRepository.filters.watchlistShowsUpcoming
    settingsRepository.filters.watchlistShowsUpcoming = when (current) {
      UpcomingFilter.OFF -> UpcomingFilter.UPCOMING
      UpcomingFilter.UPCOMING -> UpcomingFilter.RELEASED
      UpcomingFilter.RELEASED -> UpcomingFilter.OFF
    }
  }
}
