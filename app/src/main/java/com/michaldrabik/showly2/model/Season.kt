package com.michaldrabik.showly2.model

import org.threeten.bp.ZonedDateTime

data class Season(
  val ids: Ids,
  val number: Int,
  val episodeCount: Int,
  val airedEpisodes: Int,
  val title: String,
  val firstAired: ZonedDateTime?,
  val overview: String,
  val episodes: List<Episode>
)
