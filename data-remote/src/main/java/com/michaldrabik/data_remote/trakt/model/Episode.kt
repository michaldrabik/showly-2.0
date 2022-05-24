package com.michaldrabik.data_remote.trakt.model

data class Episode(
  val season: Int?,
  val number: Int?,
  val title: String?,
  val ids: Ids?,
  val overview: String?,
  val rating: Float?,
  val votes: Int?,
  val comment_count: Int?,
  val first_aired: String?,
  val runtime: Int?,
  val number_abs: Int?,
  val last_watched_at: String?
)
