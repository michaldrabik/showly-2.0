package com.michaldrabik.showly2.model

data class Ids(
  val trakt: Long,
  val slug: String,
  val tvdb: Long,
  val imdb: String,
  val tmdb: Long,
  val tvrage: Long
) {

  companion object {
    val EMPTY = Ids(-1, "", -1, "", -1, -1)
  }
}