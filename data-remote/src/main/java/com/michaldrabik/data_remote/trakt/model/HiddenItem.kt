package com.michaldrabik.data_remote.trakt.model

import java.time.ZoneOffset
import java.time.ZonedDateTime

data class HiddenItem(
  val show: Show?,
  val movie: Movie?,
  val hidden_at: String?
) {

  fun hiddenAtMillis() =
    (hidden_at?.let { ZonedDateTime.parse(hidden_at) } ?: ZonedDateTime.now(ZoneOffset.UTC)).toInstant().toEpochMilli()
}
