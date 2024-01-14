package com.michaldrabik.data_remote.trakt.model

data class PersonCredit(
  val characters: List<String>?,
  val episode_count: Int?,
  val series_regular: Boolean?,
  val show: Show?,
  val movie: Movie?,
) {

  val isShow = show != null
  val isMovie = movie != null

  val year = if (isShow) show!!.year else movie!!.year
}
