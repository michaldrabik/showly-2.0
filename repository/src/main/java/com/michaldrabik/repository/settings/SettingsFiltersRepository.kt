package com.michaldrabik.repository.settings

import android.content.SharedPreferences
import com.michaldrabik.repository.utilities.BooleanPreference
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SettingsFiltersRepository @Inject constructor(
  @Named("miscPreferences") private var preferences: SharedPreferences,
) {

  companion object Key {
    private const val WATCHLIST_SHOWS_UPCOMING = "WATCHLIST_SHOWS_UPCOMING"
    private const val WATCHLIST_MOVIES_UPCOMING = "WATCHLIST_MOVIES_UPCOMING"
  }

  var watchlistShowsUpcoming by BooleanPreference(preferences, WATCHLIST_SHOWS_UPCOMING, false)
  var watchlistMoviesUpcoming by BooleanPreference(preferences, WATCHLIST_MOVIES_UPCOMING, false)
}
