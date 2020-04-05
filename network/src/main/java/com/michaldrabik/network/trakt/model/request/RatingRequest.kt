package com.michaldrabik.network.trakt.model.request

import com.michaldrabik.network.trakt.model.Ids

data class RatingRequest(
  val shows: List<RatingRequestShow>
)

data class RatingRequestShow(
  val rating: Int,
  val ids: Ids
)
