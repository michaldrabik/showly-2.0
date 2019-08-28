package com.michaldrabik.storage.repository

import android.content.SharedPreferences
import java.util.Locale.ROOT
import javax.inject.Inject
import javax.inject.Named

class ImagesRepository @Inject constructor(
  @Named("imagesPreferences") private val preferences: SharedPreferences
) {

  private fun tvdbPosterImageKey(tvdbId: Long) = "tvdb_${tvdbId}_poster_image"

  private fun tvdbFanartImageKey(tvdbId: Long) = "tvdb_${tvdbId}_fanart_image"

  fun saveImageUrl(tvdbId: Long, imageUrl: String, type: String) {
    check(tvdbId > 0)
    check(imageUrl.isNotEmpty())
    when (type.toLowerCase(ROOT)) {
      "poster" -> savePosterImageUrl(tvdbId, imageUrl)
      "fanart" -> saveFanartImageUrl(tvdbId, imageUrl)
      else -> throw IllegalArgumentException("Invalid image type.")
    }
  }

  fun removeImageUrl(tvdbId: Long, type: String) {
    check(tvdbId > 0)
    when (type.toLowerCase(ROOT)) {
      "poster" -> removePosterImageUrl(tvdbId)
      "fanart" -> removeFanartImageUrl(tvdbId)
      else -> throw IllegalArgumentException("Invalid image type.")
    }
  }

  fun getImageUrl(tvdbId: Long, type: String): String {
    check(tvdbId > 0)
    return when (type.toLowerCase(ROOT)) {
      "poster" -> preferences.getString(tvdbPosterImageKey(tvdbId), "")
      "fanart" -> preferences.getString(tvdbFanartImageKey(tvdbId), "")
      else -> throw IllegalArgumentException("Invalid image type.")
    } ?: ""
  }

  private fun savePosterImageUrl(tvdbId: Long, imageUrl: String) {
    check(tvdbId > 0)
    check(imageUrl.isNotEmpty())
    preferences.edit()
      .putString(tvdbPosterImageKey(tvdbId), imageUrl)
      .apply()
  }

  private fun removePosterImageUrl(tvdbId: Long) {
    check(tvdbId > 0)
    preferences.edit()
      .remove(tvdbPosterImageKey(tvdbId))
      .apply()
  }

  private fun saveFanartImageUrl(tvdbId: Long, imageUrl: String) {
    check(tvdbId > 0)
    check(imageUrl.isNotEmpty())
    preferences.edit()
      .putString(tvdbFanartImageKey(tvdbId), imageUrl)
      .apply()
  }

  private fun removeFanartImageUrl(tvdbId: Long) {
    check(tvdbId > 0)
    preferences.edit()
      .remove(tvdbFanartImageKey(tvdbId))
      .apply()
  }
}