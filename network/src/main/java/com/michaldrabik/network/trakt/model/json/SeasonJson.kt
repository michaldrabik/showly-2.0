package com.michaldrabik.network.trakt.model.json

data class SeasonJson(
  val ids: IdsJson?,
  val number: Int?,
  val episode_count: Int?,
  val aired_episodes: Int?,
  val title: String?,
  val first_aired: String?,
  val overview: String?,
  val episodes: List<EpisodeJson>?
)