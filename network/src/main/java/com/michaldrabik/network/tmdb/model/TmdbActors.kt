package com.michaldrabik.network.tmdb.model

data class TmdbActors(
  val id: Long,
  val cast: List<TmdbActor>?
)
