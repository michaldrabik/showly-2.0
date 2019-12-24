package com.michaldrabik.network.tvdb.model

data class TvdbActor(
  val id: Long,
  val tvdbShowId: Long,
  val name: String,
  val role: String,
  val sortOrder: Int,
  val image: String
)
