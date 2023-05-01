package com.michaldrabik.data_remote.trakt.model

data class MovieCollection(
  val ids: Ids,
  val name: String,
  val description: String,
  val privacy: String,
  val item_count: Int,
  val likes: Int,
)
