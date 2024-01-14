package com.michaldrabik.data_remote.trakt.model

data class Movie(
  val ids: Ids?,
  val title: String?,
  val year: Int?,
  val overview: String?,
  val released: String?,
  val runtime: Int?,
  val country: String?,
  val trailer: String?,
  val homepage: String?,
  val status: String?,
  val rating: Float?,
  val votes: Long?,
  val comment_count: Long?,
  val genres: List<String>?,
  val language: String?
)
