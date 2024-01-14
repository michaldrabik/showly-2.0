package com.michaldrabik.data_remote.tmdb.model

data class TmdbPeople(
  val id: Long,
  val cast: List<TmdbPerson>?,
  val crew: List<TmdbPerson>?,
)
