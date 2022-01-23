package com.michaldrabik.repository

import android.content.SharedPreferences
import com.michaldrabik.ui_model.Show
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class OnHoldItemsRepository @Inject constructor(
  @Named("progressOnHoldPreferences") private val sharedPreferences: SharedPreferences
) {

  fun addItem(show: Show) =
    sharedPreferences.edit().putLong(show.traktId.toString(), show.traktId).apply()

  fun removeItem(show: Show) =
    sharedPreferences.edit().remove(show.traktId.toString()).apply()

  fun isOnHold(show: Show) =
    sharedPreferences.contains(show.traktId.toString())
}
