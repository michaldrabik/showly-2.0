package com.michaldrabik.ui_model

data class Movie(
  val ids: Ids,
  val title: String,
  val year: Int,
  val overview: String,
  val released: String,
  val runtime: Int,
  val country: String,
  val trailer: String,
  val homepage: String,
  val language: String,
  val status: MovieStatus,
  val rating: Float,
  val votes: Long,
  val commentCount: Long,
  val genres: List<String>,
  val updatedAt: Long
) {

  val traktId = ids.trakt.id

  companion object {
    val EMPTY = Movie(
      Ids.EMPTY,
      "",
      -1,
      "",
      "",
      -1,
      "",
      "",
      "",
      "",
      MovieStatus.UNKNOWN,
      -1F,
      -1L,
      -1L,
      emptyList(),
      -1L
    )
  }
}
