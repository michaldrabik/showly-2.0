package com.michaldrabik.storage

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Named

class UserManager @Inject constructor(
  @Named("userPreferences") private val preferences: SharedPreferences
) {

  companion object {
    private const val TVDB_TOKEN_EXPIRATION_MS = 72_000_000 //20 hours

    private const val KEY_TVDB_TOKEN = "KEY_TVDB_TOKEN"
    private const val KEY_TVDB_TOKEN_TIMESTAMP = "KEY_TVDB_TOKEN_TIMESTAMP"
  }

  var tvdbToken: String
    get() = preferences.getString(KEY_TVDB_TOKEN, "") ?: ""
    set(value) {
      preferences.edit()
        .putString(KEY_TVDB_TOKEN, value)
        .apply()
      tvdbTokenTimestamp = System.currentTimeMillis()
    }

  var tvdbTokenTimestamp: Long
    get() = preferences.getLong(KEY_TVDB_TOKEN_TIMESTAMP, 0)
    set(value) {
      preferences.edit()
        .putLong(KEY_TVDB_TOKEN_TIMESTAMP, value)
        .apply()
    }

  val isTvdbAuthorized: Boolean
    get() = when {
      tvdbToken.isEmpty() -> false
      System.currentTimeMillis() - tvdbTokenTimestamp > TVDB_TOKEN_EXPIRATION_MS -> false
      else -> true
    }
}