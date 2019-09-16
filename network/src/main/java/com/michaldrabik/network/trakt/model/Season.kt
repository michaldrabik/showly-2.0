package com.michaldrabik.network.trakt.model

data class Season(
  val ids: Ids,
  val number: Int,
  val episodeCount: Int,
  val airedEpisodes: Int,
  val title: String,
  val firstAired: String,
  val episodes: List<Episode>
)