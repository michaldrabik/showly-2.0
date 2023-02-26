package com.michaldrabik.repository.settings

import android.content.SharedPreferences
import com.michaldrabik.common.Config
import com.michaldrabik.repository.utilities.StringPreference
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SettingsViewModeRepository @Inject constructor(
  @Named("miscPreferences") private var preferences: SharedPreferences,
) {

  companion object Key {
    private const val MY_SHOWS_VIEW_MODE = "MY_SHOWS_VIEW_MODE"
    private const val WATCHLIST_SHOWS_VIEW_MODE = "WATCHLIST_SHOWS_VIEW_MODE"
    private const val HIDDEN_SHOWS_VIEW_MODE = "HIDDEN_SHOWS_VIEW_MODE"

    private const val MY_MOVIES_VIEW_MODE = "MY_MOVIES_VIEW_MODE"
    private const val WATCHLIST_MOVIES_VIEW_MODE = "WATCHLIST_MOVIES_VIEW_MODE"
    private const val HIDDEN_MOVIES_VIEW_MODE = "HIDDEN_MOVIES_VIEW_MODE"

    private const val CUSTOM_LIST_VIEW_MODE = "CUSTOM_LIST_VIEW_MODE"
  }

  var myShowsViewMode by StringPreference(preferences, MY_SHOWS_VIEW_MODE, Config.DEFAULT_LIST_VIEW_MODE)
  var watchlistShowsViewMode by StringPreference(preferences, WATCHLIST_SHOWS_VIEW_MODE, Config.DEFAULT_LIST_VIEW_MODE)
  var hiddenShowsViewMode by StringPreference(preferences, HIDDEN_SHOWS_VIEW_MODE, Config.DEFAULT_LIST_VIEW_MODE)

  var myMoviesViewMode by StringPreference(preferences, MY_MOVIES_VIEW_MODE, Config.DEFAULT_LIST_VIEW_MODE)
  var watchlistMoviesViewMode by StringPreference(preferences, WATCHLIST_MOVIES_VIEW_MODE, Config.DEFAULT_LIST_VIEW_MODE)
  var hiddenMoviesViewMode by StringPreference(preferences, HIDDEN_MOVIES_VIEW_MODE, Config.DEFAULT_LIST_VIEW_MODE)

  var customListsViewMode by StringPreference(preferences, CUSTOM_LIST_VIEW_MODE, Config.DEFAULT_LIST_VIEW_MODE)
}
