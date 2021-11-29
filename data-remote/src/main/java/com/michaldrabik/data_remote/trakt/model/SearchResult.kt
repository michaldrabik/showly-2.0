package com.michaldrabik.data_remote.trakt.model

data class SearchResult(
  val score: Float?,
  val show: Show?,
  val movie: Movie?,
  val person: Person?
) {

  fun getVotes() = when {
    show != null -> show.votes ?: 0
    movie != null -> movie.votes ?: 0
    else -> 0
  }
}
