package com.michaldrabik.data_remote.tmdb.model

data class TmdbTranslationResponse(
  val id: Long?,
  val translations: List<TmdbTranslation>?
)
