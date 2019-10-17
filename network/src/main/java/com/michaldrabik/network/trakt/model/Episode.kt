package com.michaldrabik.network.trakt.model

data class Episode(
  val season: Int,
  val number: Int,
  val title: String,
  val ids: Ids,
  val overview: String,
  val rating: Float,
  val votes: Int,
  val commentCount: Int,
  val firstAired: String,
  val runtime: Int
)