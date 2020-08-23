package com.michaldrabik.showly2.model

import com.michaldrabik.showly2.utilities.extensions.nowUtc
import org.threeten.bp.ZonedDateTime

data class TraktRating(
  val idTrakt: IdTrakt,
  val rating: Int,
  val ratedAt: ZonedDateTime = nowUtc()
) {
  companion object {
    val EMPTY = TraktRating(IdTrakt(-1), 0)
  }
}
