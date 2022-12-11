package com.michaldrabik.repository.settings

import android.content.SharedPreferences
import com.michaldrabik.repository.utilities.BooleanPreference
import com.michaldrabik.repository.utilities.EnumPreference
import com.michaldrabik.ui_model.MyShowsSection
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SettingsFiltersRepository @Inject constructor(
  @Named("miscPreferences") private var preferences: SharedPreferences,
) {

  companion object Key {
    private const val MY_SHOWS_TYPE = "MY_SHOWS_TYPE"
    private const val WATCHLIST_SHOWS_UPCOMING = "WATCHLIST_SHOWS_UPCOMING"
    private const val WATCHLIST_MOVIES_UPCOMING = "WATCHLIST_MOVIES_UPCOMING"
  }

  var myShowsType by EnumPreference(preferences, MY_SHOWS_TYPE, MyShowsSection.ALL, MyShowsSection::class.java)

  var watchlistShowsUpcoming by BooleanPreference(preferences, WATCHLIST_SHOWS_UPCOMING, false)
  var watchlistMoviesUpcoming by BooleanPreference(preferences, WATCHLIST_MOVIES_UPCOMING, false)
}
