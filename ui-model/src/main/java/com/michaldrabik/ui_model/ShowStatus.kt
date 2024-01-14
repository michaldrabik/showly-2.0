package com.michaldrabik.ui_model

import androidx.annotation.StringRes

enum class ShowStatus(
  val key: String,
  @StringRes val displayName: Int
) {
  RETURNING("returning series", R.string.textShowStatusReturning),
  UPCOMING("upcoming", R.string.textShowStatusUpcoming),
  IN_PRODUCTION("in production", R.string.textShowStatusInProduction),
  PLANNED("planned", R.string.textShowStatusPlanned),
  CANCELED("canceled", R.string.textShowStatusCanceled),
  ENDED("ended", R.string.textShowStatusEnded),
  UNKNOWN("unknown", R.string.textShowStatusUnknown);

  fun isAnticipated() = this in arrayOf(UPCOMING, IN_PRODUCTION, PLANNED)

  companion object {
    fun fromKey(key: String?) =
      enumValues<ShowStatus>().firstOrNull { it.key == key } ?: UNKNOWN
  }
}
