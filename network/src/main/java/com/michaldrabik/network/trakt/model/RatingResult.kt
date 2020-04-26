package com.michaldrabik.network.trakt.model

data class RatingResult(
  val rating: Int,
  val show: RatingResultShow
)

data class RatingResultShow(
  val title: String,
  val ids: Ids
)
