package com.michaldrabik.ui_repository

import android.content.SharedPreferences
import com.michaldrabik.common.di.AppScope
import javax.inject.Inject
import javax.inject.Named

@AppScope
class PinnedItemsRepository @Inject constructor(
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
