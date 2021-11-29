package com.michaldrabik.data_remote.tmdb.model

data class TmdbTranslation(
  val iso_639_1: String,
  val data: Data?
) {

  data class Data(
    val biography: String?
  )
}
