package com.michaldrabik.data_remote.trakt.model

data class Season(
  val ids: Ids?,
  val number: Int?,
  val episode_count: Int?,
  val aired_episodes: Int?,
  val title: String?,
  val first_aired: String?,
  val overview: String?,
  val rating: Float?,
  val episodes: List<Episode>?
)
