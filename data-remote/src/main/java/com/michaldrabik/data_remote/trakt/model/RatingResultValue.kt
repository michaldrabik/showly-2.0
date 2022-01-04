package com.michaldrabik.data_remote.trakt.model

data class RatingResultValue(
  val ids: Ids,
  val title: String,
  val season: Int?,
  val number: Int?,
)
