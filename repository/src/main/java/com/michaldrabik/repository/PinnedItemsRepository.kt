package com.michaldrabik.repository

import android.content.SharedPreferences
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class PinnedItemsRepository @Inject constructor(
  @Named("watchlistPreferences") private val sharedPreferences: SharedPreferences,
  @Named("progressMoviesPreferences") private val sharedPreferencesMovies: SharedPreferences
) {

  fun addPinnedItem(show: Show) =
    sharedPreferences.edit().putLong(show.traktId.toString(), show.traktId).apply()

  fun addPinnedItem(movie: Movie) =
    sharedPreferencesMovies.edit().putLong(movie.traktId.toString(), movie.traktId).apply()

  fun removePinnedItem(show: Show) =
    sharedPreferences.edit().remove(show.traktId.toString()).apply()

  fun removePinnedItem(movie: Movie) =
    sharedPreferencesMovies.edit().remove(movie.traktId.toString()).apply()

  fun isItemPinned(show: Show) =
    sharedPreferences.contains(show.traktId.toString())

  fun isItemPinned(movie: Movie) =
    sharedPreferencesMovies.contains(movie.traktId.toString())
}
