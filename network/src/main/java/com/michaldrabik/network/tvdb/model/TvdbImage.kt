package com.michaldrabik.network.tvdb.model

data class TvdbImage(
  val id: Long?,
  val fileName: String?,
  val thumbnail: String?,
  val keyType: String?,
  val ratingsInfo: TvdbImageRating?
)
