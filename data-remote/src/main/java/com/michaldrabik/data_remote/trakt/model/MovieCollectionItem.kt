package com.michaldrabik.data_remote.trakt.model

data class MovieCollectionItem(
  val id: Long,
  val rank: Int,
  val movie: Movie,
)
