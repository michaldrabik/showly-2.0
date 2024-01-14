package com.michaldrabik.data_remote.trakt.model

data class SeasonTranslation(
  val season: Int,
  val number: Int,
  val ids: Ids,
  val translations: List<Translation>?
)
