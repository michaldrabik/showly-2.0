package com.michaldrabik.data_remote.omdb.model

data class OmdbResult(
  val Ratings: List<OmdbRating>?,
  val imdbRating: String?,
  val imdbVotes: String?,
  val Metascore: String?,
  val tomatoURL: String?,
)

data class OmdbRating(
  val Source: String?,
  val Value: String?,
)
