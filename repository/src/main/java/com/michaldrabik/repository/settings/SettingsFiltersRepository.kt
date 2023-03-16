package com.michaldrabik.repository.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import com.michaldrabik.repository.utilities.BooleanPreference
import com.michaldrabik.repository.utilities.EnumPreference
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.Network
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SettingsFiltersRepository @Inject constructor(
  @Named("miscPreferences") private var preferences: SharedPreferences,
) {

  companion object Key {
    private const val MY_SHOWS_TYPE = "MY_SHOWS_TYPE"
    private const val MY_SHOWS_NETWORKS = "MY_SHOWS_NETWORKS"
    private const val WATCHLIST_SHOWS_UPCOMING = "WATCHLIST_SHOWS_UPCOMING"
    private const val WATCHLIST_SHOWS_NETWORKS = "WATCHLIST_SHOWS_NETWORKS"
    private const val HIDDEN_SHOWS_NETWORKS = "HIDDEN_SHOWS_NETWORKS"
    private const val WATCHLIST_MOVIES_UPCOMING = "WATCHLIST_MOVIES_UPCOMING"
  }

  var myShowsType by EnumPreference(preferences, MY_SHOWS_TYPE, MyShowsSection.ALL, MyShowsSection::class.java)
  var myShowsNetworks: List<Network>
    get() {
      val filters = preferences.getStringSet(MY_SHOWS_NETWORKS, emptySet()) ?: emptySet()
      return filters.map { Network.valueOf(it) }
    }
    set(value) {
      preferences.edit { putStringSet(MY_SHOWS_NETWORKS, value.map { it.name }.toSet()) }
    }

  var watchlistShowsUpcoming by BooleanPreference(preferences, WATCHLIST_SHOWS_UPCOMING, false)
  var watchlistShowsNetworks: List<Network>
    get() {
      val filters = preferences.getStringSet(WATCHLIST_SHOWS_NETWORKS, emptySet()) ?: emptySet()
      return filters.map { Network.valueOf(it) }
    }
    set(value) {
      preferences.edit { putStringSet(WATCHLIST_SHOWS_NETWORKS, value.map { it.name }.toSet()) }
    }
  var hiddenShowsNetworks: List<Network>
    get() {
      val filters = preferences.getStringSet(HIDDEN_SHOWS_NETWORKS, emptySet()) ?: emptySet()
      return filters.map { Network.valueOf(it) }
    }
    set(value) {
      preferences.edit { putStringSet(HIDDEN_SHOWS_NETWORKS, value.map { it.name }.toSet()) }
    }

  var watchlistMoviesUpcoming by BooleanPreference(preferences, WATCHLIST_MOVIES_UPCOMING, false)
}
