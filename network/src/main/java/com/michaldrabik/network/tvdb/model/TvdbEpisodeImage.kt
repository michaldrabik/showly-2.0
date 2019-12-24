package com.michaldrabik.network.tvdb.model

data class TvdbEpisodeImageResult(
  val data: TvdbEpisodeImage
)

data class TvdbEpisodeImage(
  val id: Long?,
  val filename: String?
)
