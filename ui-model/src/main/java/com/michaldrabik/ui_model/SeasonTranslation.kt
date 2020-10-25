package com.michaldrabik.ui_model

data class SeasonTranslation(
  val ids: Ids,
  val title: String,
  val seasonNumber: Int,
  val episodeNumber: Int,
  val overview: String,
  val language: String,
  val isLocal: Boolean = false
)
