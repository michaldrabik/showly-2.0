package com.michaldrabik.data_remote.tmdb.model

data class TmdbPerson(
  val id: Long,
  val name: String?,
  val place_of_birth: String?,
  val homepage: String?,
  val character: String?,
  val deathday: String?,
  val birthday: String?,
  val biography: String?,
  val imdb_id: String?,
  val known_for_department: String?,
  val profile_path: String?
)
