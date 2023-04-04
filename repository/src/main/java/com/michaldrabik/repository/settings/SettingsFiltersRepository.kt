package com.michaldrabik.repository.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import com.michaldrabik.repository.utilities.BooleanPreference
import com.michaldrabik.repository.utilities.EnumPreference
import com.michaldrabik.ui_model.Genre
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
    private const val PROGRESS_SHOWS_UPCOMING = "PROGRESS_SHOWS_UPCOMING"
    private const val PROGRESS_SHOWS_ON_HOLD = "PROGRESS_SHOWS_ON_HOLD"
    private const val MY_SHOWS_TYPE = "MY_SHOWS_TYPE"
    private const val MY_SHOWS_NETWORKS = "MY_SHOWS_NETWORKS"
    private const val MY_SHOWS_GENRES = "MY_SHOWS_GENRES"
    private const val WATCHLIST_SHOWS_UPCOMING = "WATCHLIST_SHOWS_UPCOMING"
    private const val WATCHLIST_SHOWS_NETWORKS = "WATCHLIST_SHOWS_NETWORKS"
    private const val WATCHLIST_SHOWS_GENRES = "WATCHLIST_SHOWS_GENRES"
    private const val HIDDEN_SHOWS_NETWORKS = "HIDDEN_SHOWS_NETWORKS"
    private const val HIDDEN_SHOWS_GENRES = "HIDDEN_SHOWS_GENRES"

    private const val MY_MOVIES_GENRES = "MY_MOVIES_GENRES"
    private const val WATCHLIST_MOVIES_UPCOMING = "WATCHLIST_MOVIES_UPCOMING"
    private const val WATCHLIST_MOVIES_GENRES = "WATCHLIST_MOVIES_GENRES"
    private const val HIDDEN_MOVIES_GENRES = "HIDDEN_MOVIES_GENRES"
  }

  // Shows

  var progressShowsUpcoming by BooleanPreference(preferences, PROGRESS_SHOWS_UPCOMING, false)
  var progressShowsOnHold by BooleanPreference(preferences, PROGRESS_SHOWS_ON_HOLD, false)

  var myShowsType by EnumPreference(preferences, MY_SHOWS_TYPE, MyShowsSection.ALL, MyShowsSection::class.java)
  var myShowsNetworks: List<Network>
    get() {
      val filters = preferences.getStringSet(MY_SHOWS_NETWORKS, emptySet()) ?: emptySet()
      return filters.map { Network.valueOf(it) }
    }
    set(value) {
      preferences.edit { putStringSet(MY_SHOWS_NETWORKS, value.map { it.name }.toSet()) }
    }
  var myShowsGenres: List<Genre>
    get() {
      val filters = preferences.getStringSet(MY_SHOWS_GENRES, emptySet()) ?: emptySet()
      return filters.map { Genre.valueOf(it) }
    }
    set(value) {
      preferences.edit { putStringSet(MY_SHOWS_GENRES, value.map { it.name }.toSet()) }
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
  var watchlistShowsGenres: List<Genre>
    get() {
      val filters = preferences.getStringSet(WATCHLIST_SHOWS_GENRES, emptySet()) ?: emptySet()
      return filters.map { Genre.valueOf(it) }
    }
    set(value) {
      preferences.edit { putStringSet(WATCHLIST_SHOWS_GENRES, value.map { it.name }.toSet()) }
    }

  var hiddenShowsNetworks: List<Network>
    get() {
      val filters = preferences.getStringSet(HIDDEN_SHOWS_NETWORKS, emptySet()) ?: emptySet()
      return filters.map { Network.valueOf(it) }
    }
    set(value) {
      preferences.edit { putStringSet(HIDDEN_SHOWS_NETWORKS, value.map { it.name }.toSet()) }
    }
  var hiddenShowsGenres: List<Genre>
    get() {
      val filters = preferences.getStringSet(HIDDEN_SHOWS_GENRES, emptySet()) ?: emptySet()
      return filters.map { Genre.valueOf(it) }
    }
    set(value) {
      preferences.edit { putStringSet(HIDDEN_SHOWS_GENRES, value.map { it.name }.toSet()) }
    }

  // Movies

  var myMoviesGenres: List<Genre>
    get() {
      val filters = preferences.getStringSet(MY_MOVIES_GENRES, emptySet()) ?: emptySet()
      return filters.map { Genre.valueOf(it) }
    }
    set(value) {
      preferences.edit { putStringSet(MY_MOVIES_GENRES, value.map { it.name }.toSet()) }
    }

  var watchlistMoviesUpcoming by BooleanPreference(preferences, WATCHLIST_MOVIES_UPCOMING, false)
  var watchlistMoviesGenres: List<Genre>
    get() {
      val filters = preferences.getStringSet(WATCHLIST_MOVIES_GENRES, emptySet()) ?: emptySet()
      return filters.map { Genre.valueOf(it) }
    }
    set(value) {
      preferences.edit { putStringSet(WATCHLIST_MOVIES_GENRES, value.map { it.name }.toSet()) }
    }

  var hiddenMoviesGenres: List<Genre>
    get() {
      val filters = preferences.getStringSet(HIDDEN_MOVIES_GENRES, emptySet()) ?: emptySet()
      return filters.map { Genre.valueOf(it) }
    }
    set(value) {
      preferences.edit { putStringSet(HIDDEN_MOVIES_GENRES, value.map { it.name }.toSet()) }
    }
}
