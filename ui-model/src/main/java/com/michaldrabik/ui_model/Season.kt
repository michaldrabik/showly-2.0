package com.michaldrabik.ui_model

import java.time.ZonedDateTime

data class Season(
  val ids: Ids,
  val number: Int,
  val episodeCount: Int,
  val airedEpisodes: Int,
  val title: String,
  val firstAired: ZonedDateTime?,
  val overview: String,
  val rating: Float,
  val episodes: List<Episode>
) {

  companion object {
    val EMPTY = Season(
      ids = Ids.EMPTY,
      number = 0,
      episodeCount = 0,
      airedEpisodes = 0,
      title = "",
      firstAired = null,
      overview = "",
      rating = 0F,
      episodes = listOf()
    )
  }

  fun isSpecial() = number == 0
}
