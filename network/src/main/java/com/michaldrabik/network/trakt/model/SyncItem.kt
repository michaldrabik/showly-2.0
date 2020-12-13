package com.michaldrabik.network.trakt.model

import org.threeten.bp.ZoneOffset.UTC
import org.threeten.bp.ZonedDateTime

data class SyncItem(
  val show: Show?,
  val movie: Movie?,
  val seasons: List<Season>?,
  val last_watched_at: String?,
  val last_updated_at: String?
) {

  fun lastWatchedMillis() =
    (last_watched_at?.let { ZonedDateTime.parse(last_watched_at) } ?: ZonedDateTime.now(UTC)).toInstant().toEpochMilli()

  fun lastUpdateMillis() =
    (last_updated_at?.let { ZonedDateTime.parse(last_updated_at) } ?: ZonedDateTime.now(UTC)).toInstant().toEpochMilli()
}
