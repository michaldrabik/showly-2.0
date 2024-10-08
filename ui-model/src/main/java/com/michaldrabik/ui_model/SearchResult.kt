package com.michaldrabik.ui_model

data class SearchResult(
  val order: Int,
  val show: Show,
  val movie: Movie,
) {

  val isShow = show != Show.EMPTY

  val traktId = if (show != Show.EMPTY) show.traktId else movie.traktId
}
