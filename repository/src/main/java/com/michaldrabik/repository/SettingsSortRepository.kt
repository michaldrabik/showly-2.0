package com.michaldrabik.repository

import android.content.SharedPreferences
import com.michaldrabik.common.delegates.EnumPreference
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SettingsSortRepository @Inject constructor(
  @Named("miscPreferences") private var preferences: SharedPreferences
) {

  companion object Key {
    private const val PROGRESS_SHOWS_SORT_ORDER = "PROGRESS_SHOWS_SORT_ORDER"
    private const val PROGRESS_SHOWS_SORT_TYPE = "PROGRESS_SHOWS_SORT_TYPE"
  }

  var progressShowsSortOrder by EnumPreference(preferences, PROGRESS_SHOWS_SORT_ORDER, SortOrder.NAME, SortOrder::class.java)
  var progressShowsSortType by EnumPreference(preferences, PROGRESS_SHOWS_SORT_TYPE, SortType.ASCENDING, SortType::class.java)
}
