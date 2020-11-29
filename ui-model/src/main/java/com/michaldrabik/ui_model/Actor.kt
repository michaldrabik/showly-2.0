package com.michaldrabik.ui_model

data class Actor(
  val id: Long,
  val imdbId: String?,
  val tvdbShowId: Long,
  val tmdbMovieId: Long,
  val name: String,
  val role: String,
  val sortOrder: Int,
  val image: String
)
