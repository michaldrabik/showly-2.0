package com.michaldrabik.network.trakt.model.json

data class IdsJson(
  val trakt: Long?,
  val slug: String?,
  val tvdb: Long?,
  val imdb: String?,
  val tmdb: Long?,
  val tvrage: Long?
)
