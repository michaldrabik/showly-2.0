package com.michaldrabik.showly2.model

data class TraktRating(
  val idTrakt: IdTrakt,
  val rating: Int
) {
  companion object {
    val EMPTY = TraktRating(IdTrakt(-1), 0)
  }
}
