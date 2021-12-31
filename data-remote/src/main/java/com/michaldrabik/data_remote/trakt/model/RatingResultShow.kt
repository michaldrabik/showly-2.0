package com.michaldrabik.data_remote.trakt.model

data class RatingResultShow(
  val rated_at: String?,
  val rating: Int,
  val show: RatingResultValue
)

data class RatingResultEpisode(
  val rating: Int,
  val rated_at: String?,
  val episode: RatingResultValue,
  val show: RatingResultValue
)

data class RatingResultSeason(
  val rating: Int,
  val rated_at: String?,
  val season: RatingResultValue,
  val show: RatingResultValue
)
