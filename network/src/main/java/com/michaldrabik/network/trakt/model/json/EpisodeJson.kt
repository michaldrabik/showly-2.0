package com.michaldrabik.network.trakt.model.json

data class EpisodeJson(
  val season: Int?,
  val number: Int?,
  val title: String?,
  val ids: IdsJson?,
  val overview: String?,
  val rating: Float?,
  val votes: Int?,
  val comment_count: Int?,
  val first_aired: String?,
  val updated_at: String?,
  val runtime: Int?
)