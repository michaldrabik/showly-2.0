package com.michaldrabik.network.trakt.model

data class SearchResult(
  val score: Float?,
  val show: Show?,
  val movie: Movie?
) {

  val votes = show?.votes ?: movie?.votes
}
