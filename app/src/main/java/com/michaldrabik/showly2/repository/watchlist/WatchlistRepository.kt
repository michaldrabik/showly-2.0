package com.michaldrabik.showly2.repository.watchlist

import android.content.SharedPreferences
import com.michaldrabik.showly2.di.scope.AppScope
import javax.inject.Inject
import javax.inject.Named

@AppScope
class WatchlistRepository @Inject constructor(
  @Named("watchlistPreferences") private val sharedPreferences: SharedPreferences
) {

  fun addPinnedItem(itemTraktId: Long) {
    sharedPreferences.edit().putLong(itemTraktId.toString(), itemTraktId).apply()
  }

  fun removePinnedItem(itemTraktId: Long) {
    sharedPreferences.edit().remove(itemTraktId.toString()).apply()
  }

  fun isItemPinned(itemTraktId: Long) =
    sharedPreferences.contains(itemTraktId.toString())
}
