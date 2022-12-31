package com.michaldrabik.repository.settings

import android.content.SharedPreferences
import com.michaldrabik.repository.utilities.StringPreference
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SettingsViewModeRepository @Inject constructor(
  @Named("miscPreferences") private var preferences: SharedPreferences,
) {

  companion object Key {
    private const val WATCHLIST_SHOWS_VIEW_MODE = "WATCHLIST_SHOWS_VIEW_MODE"
    private const val WATCHLIST_MOVIES_VIEW_MODE = "WATCHLIST_MOVIES_VIEW_MODE"
  }

  var watchlistShowsViewMode by StringPreference(preferences, WATCHLIST_SHOWS_VIEW_MODE, "LIST_NORMAL")
  var watchlistMoviesViewMode by StringPreference(preferences, WATCHLIST_MOVIES_VIEW_MODE, "LIST_NORMAL")
}
