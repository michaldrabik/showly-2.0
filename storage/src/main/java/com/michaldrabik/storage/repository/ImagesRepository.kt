package com.michaldrabik.storage.repository

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Named

class ImagesRepository @Inject constructor(
  @Named("imagesPreferences") private val preferences: SharedPreferences
) {

  private fun tvdbPosterImageKey(tvdbId: Long) = "tvdb_${tvdbId}_poster_image"

  fun savePosterImageUrl(tvdbId: Long, imageUrl: String) {
    check(tvdbId > 0)
    check(imageUrl.isNotEmpty())
    preferences.edit()
      .putString(tvdbPosterImageKey(tvdbId), imageUrl)
      .apply()
  }

  fun removePosterImageUrl(tvdbId: Long) {
    check(tvdbId > 0)
    preferences.edit()
      .remove(tvdbPosterImageKey(tvdbId))
      .apply()
  }

  fun getPosterImageUrl(tvdbId: Long): String =
    preferences.getString(tvdbPosterImageKey(tvdbId), "") ?: ""
}