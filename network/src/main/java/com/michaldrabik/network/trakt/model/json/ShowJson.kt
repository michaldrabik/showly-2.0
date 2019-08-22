package com.michaldrabik.network.trakt.model.json

data class ShowJson(
  val title: String?,
  val year: Int?,
  val ids: IdsJson?,
  val overview: String?,
  val first_aired: String?,
  val airs: AirTimeJson?,
  val runtime: Int?,
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