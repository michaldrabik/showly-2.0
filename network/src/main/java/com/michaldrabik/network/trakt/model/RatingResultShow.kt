package com.michaldrabik.network.trakt.model

data class RatingResultShow(
  val rating: Int,
  val show: RatingResultValue
)

data class RatingResultEpisode(
  val rating: Int,
  val episode: RatingResultValue
)

data class RatingResultValue(
  val title: String,
  val ids: Ids
)
