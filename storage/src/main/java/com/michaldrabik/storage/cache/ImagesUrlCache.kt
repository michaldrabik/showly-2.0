package com.michaldrabik.storage.cache

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Named

/**
 * This class holds information on valid images URLs that have been acquired via remote service (TVDB).
 */
class ImagesUrlCache @Inject constructor(
  @Named("imagesPreferences") private val preferences: SharedPreferences
) {

  private val allowedKeys = arrayOf("poster", "fanart")

  private fun tvdbImageKey(tvdbId: Long, type: String) = "tvdb_${tvdbId}_${type}"

  fun saveImageUrl(tvdbId: Long, imageUrl: String, type: String) {
    check(tvdbId > 0)
    check(imageUrl.isNotEmpty())
    check(type in allowedKeys)
    preferences.edit()
      .putString(tvdbImageKey(tvdbId, type), imageUrl)
      .apply()
  }

  fun removeImageUrl(tvdbId: Long, type: String) {
    check(tvdbId > 0)
    check(type in allowedKeys)
    preferences.edit()
      .remove(tvdbImageKey(tvdbId, type))
      .apply()
  }

  fun getImageUrl(tvdbId: Long, type: String): String {
    check(tvdbId > 0)
    check(type in allowedKeys)
    return preferences.getString(tvdbImageKey(tvdbId, type), "") ?: ""
  }
}