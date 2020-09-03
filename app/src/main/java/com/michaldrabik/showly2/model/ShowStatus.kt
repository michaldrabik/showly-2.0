package com.michaldrabik.showly2.model

enum class ShowStatus(
  val key: String,
  val displayName: String
) {
  RETURNING("returning series", "Returning Series"),
  UPCOMING("upcoming", "Upcoming"),
  IN_PRODUCTION("in production", "In Production"),
  PLANNED("planned", "Planned"),
  CANCELED("canceled", "Canceled"),
  ENDED("ended", "Ended"),
  UNKNOWN("unknown", "");

  companion object {
    fun fromKey(key: String?) =
      enumValues<ShowStatus>().firstOrNull { it.key == key } ?: UNKNOWN
  }
}
