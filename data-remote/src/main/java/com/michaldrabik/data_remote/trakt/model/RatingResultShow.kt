package com.michaldrabik.data_remote.trakt.model

data class RatingResultShow(
  val rated_at: String?,
  val rating: Int,
  val show: RatingResultValue
)

data class RatingResultEpisode(
  val rating: Int,
  val episode: RatingResultValue
)
