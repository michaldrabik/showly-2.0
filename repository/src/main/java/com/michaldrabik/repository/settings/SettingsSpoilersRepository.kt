package com.michaldrabik.repository.settings

import android.content.SharedPreferences
import com.michaldrabik.repository.utilities.BooleanPreference
import com.michaldrabik.ui_model.SpoilersSettings
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SettingsSpoilersRepository @Inject constructor(
  @Named("spoilersPreferences") private var preferences: SharedPreferences,
) {

  companion object Key {
    private const val SHOWS_UNCOLLECTED_SHOWS_HIDDEN = "SHOWS_UNCOLLECTED_SHOWS_HIDDEN"
    private const val SHOWS_MY_SHOWS_HIDDEN = "SHOWS_MY_SHOWS_HIDDEN"
    private const val SHOWS_WATCHLIST_SHOWS_HIDDEN = "SHOWS_WATCHLIST_SHOWS_HIDDEN"
    private const val SHOWS_HIDDEN_SHOWS_HIDDEN = "SHOWS_HIDDEN_SHOWS_HIDDEN"

    private const val MOVIES_UNCOLLECTED_MOVIES_HIDDEN = "MOVIES_UNCOLLECTED_MOVIES_HIDDEN"
    private const val MOVIES_MY_MOVIES_HIDDEN = "MOVIES_MY_MOVIES_HIDDEN"
    private const val MOVIES_WATCHLIST_MOVIES_HIDDEN = "MOVIES_WATCHLIST_MOVIES_HIDDEN"
    private const val MOVIES_HIDDEN_MOVIES_HIDDEN = "MOVIES_HIDDEN_MOVIES_HIDDEN"
  }

  var isMyShowsHidden by BooleanPreference(preferences, SHOWS_MY_SHOWS_HIDDEN, false)
  var isWatchlistShowsHidden by BooleanPreference(preferences, SHOWS_WATCHLIST_SHOWS_HIDDEN, false)
  var isHiddenShowsHidden by BooleanPreference(preferences, SHOWS_HIDDEN_SHOWS_HIDDEN, false)
  var isUncollectedShowsHidden by BooleanPreference(preferences, SHOWS_UNCOLLECTED_SHOWS_HIDDEN, false)

  var isMyMoviesHidden by BooleanPreference(preferences, MOVIES_MY_MOVIES_HIDDEN, false)
  var isWatchlistMoviesHidden by BooleanPreference(preferences, MOVIES_WATCHLIST_MOVIES_HIDDEN, false)
  var isHiddenMoviesHidden by BooleanPreference(preferences, MOVIES_HIDDEN_MOVIES_HIDDEN, false)
  var isUncollectedMoviesHidden by BooleanPreference(preferences, MOVIES_UNCOLLECTED_MOVIES_HIDDEN, false)

  fun getAll(): SpoilersSettings = SpoilersSettings(
    isMyShowsHidden = isMyShowsHidden,
    isMyMoviesHidden = isMyMoviesHidden,
    isWatchlistShowsHidden = isWatchlistShowsHidden,
    isWatchlistMoviesHidden = isWatchlistMoviesHidden,
    isHiddenShowsHidden = isHiddenShowsHidden,
    isHiddenMoviesHidden = isHiddenMoviesHidden,
    isNotCollectedShowsHidden = isUncollectedShowsHidden,
    isNotCollectedMoviesHidden = isUncollectedMoviesHidden,
  )
}
