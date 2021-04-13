package com.michaldrabik.data_remote.tmdb.model

data class TmdbActor(
  val id: Long,
  val showTmdbId: Long,
  val movieTmdbId: Long,
  val name: String?,
  val character: String?,
  val order: Int,
  val profile_path: String?
)
