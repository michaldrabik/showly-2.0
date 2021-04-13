package com.michaldrabik.data_remote.trakt.model

data class Show(
  val ids: Ids?,
  val title: String?,
  val year: Int?,
  val overview: String?,
  val first_aired: String?,
  val runtime: Int?,
  val airs: AirTime?,
  val certification: String?,
  val network: String?,
  val country: String?,
  val trailer: String?,
  val homepage: String?,
  val status: String?,
  val rating: Float?,
  val votes: Long?,
  val comment_count: Long?,
  val genres: List<String>?,
  val aired_episodes: Int?
)
