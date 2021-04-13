package com.michaldrabik.data_remote.tmdb.model

data class TmdbActors(
  val id: Long,
  val cast: List<TmdbActor>?
)
