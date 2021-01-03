package com.michaldrabik.ui_model

data class Actor(
  val tvdbId: Long,
  val tmdbId: Long,
  val imdbId: String?,
  val tvdbShowId: Long,
  val tmdbMovieId: Long,
  val tmdbShowId: Long,
  val name: String,
  val role: String,
  val sortOrder: Int,
  val image: String
)
