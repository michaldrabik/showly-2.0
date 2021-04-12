package com.michaldrabik.ui_model

import androidx.annotation.StringRes

enum class MovieStatus(
  val key: String,
  @StringRes val displayName: Int
) {
  RELEASED("released", R.string.textMovieStatusReleased),
  IN_PRODUCTION("in production", R.string.textMovieStatusInProduction),
  POST_PRODUCTION("post production", R.string.textMovieStatusPostProduction),
  PLANNED("planned", R.string.textMovieStatusPlanned),
  RUMORED("rumored", R.string.textMovieStatusRumored),
  CANCELED("canceled", R.string.textMovieStatusCanceled),
  UNKNOWN("unknown", R.string.textMovieStatusUnknown);

  fun isAnticipated() = this in arrayOf(IN_PRODUCTION, POST_PRODUCTION, PLANNED, RUMORED)

  companion object {
    fun fromKey(key: String?) =
      enumValues<MovieStatus>().firstOrNull { it.key == key } ?: UNKNOWN
  }
}
