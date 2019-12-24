package com.michaldrabik.network.trakt.model

data class Ids(
  val trakt: Long,
  val tvdb: Long,
  val tmdb: Long,
  val tvrage: Long,
  val imdb: String,
  val slug: String
)
