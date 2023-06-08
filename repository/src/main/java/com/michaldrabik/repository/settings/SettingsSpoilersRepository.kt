package com.michaldrabik.repository.settings

import android.content.SharedPreferences
import com.michaldrabik.repository.utilities.BooleanPreference
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SettingsSpoilersRepository @Inject constructor(
  @Named("spoilersPreferences") private var preferences: SharedPreferences,
) {

  companion object Key {
    private const val SHOWS_DETAILS_HIDDEN = "SHOWS_DETAILS_HIDDEN"
    private const val SHOWS_LISTS_HIDDEN = "SHOWS_LISTS_HIDDEN"
  }

  var isShowsDetailsHidden by BooleanPreference(preferences, SHOWS_DETAILS_HIDDEN, false)
  var isShowsListsHidden by BooleanPreference(preferences, SHOWS_LISTS_HIDDEN, false)
}
