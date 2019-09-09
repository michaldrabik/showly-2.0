package com.michaldrabik.network.tvdb.model.json

data class TvdbActorJson(
  val id: Long?,
  val seriesId: Long?,
  val name: String?,
  val role: String?,
  val sortOrder: Int?,
  val image: String?
)