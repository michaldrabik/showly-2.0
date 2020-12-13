package com.michaldrabik.network.trakt.model.request

import com.michaldrabik.network.trakt.model.Ids

data class RatingRequest(
  val shows: List<RatingRequestValue>? = null,
  val movies: List<RatingRequestValue>? = null,
  val episodes: List<RatingRequestValue>? = null
)

data class RatingRequestValue(
  val rating: Int,
  val ids: Ids?
)
