package com.michaldrabik.data_remote.trakt.model

data class RatingResultMovie(
  val rated_at: String?,
  val rating: Int,
  val movie: RatingResultValue
)
