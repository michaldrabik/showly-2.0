package com.michaldrabik.data_remote.trakt.model

import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

data class SyncItem(
  val show: Show?,
  val movie: Movie?,
  val seasons: List<Season>?,
  val last_watched_at: String?,
  val last_updated_at: String?,
  val listed_at: String?
) {

  fun getTraktId(): Long? {
    if (show != null) return show.ids?.trakt
    if (movie != null) return movie.ids?.trakt
    return null
  }

  fun getType(): String? {
    if (show != null) return "show"
    if (movie != null) return "movie"
    return null
  }

  fun lastWatchedMillis() =
    (last_watched_at?.let { ZonedDateTime.parse(last_watched_at) } ?: ZonedDateTime.now(UTC)).toInstant().toEpochMilli()

  fun lastUpdateMillis() =
    (last_updated_at?.let { ZonedDateTime.parse(last_updated_at) } ?: ZonedDateTime.now(UTC)).toInstant().toEpochMilli()

  fun lastListedMillis() =
    (listed_at?.let { ZonedDateTime.parse(listed_at) } ?: ZonedDateTime.now(UTC)).toInstant().toEpochMilli()
}
