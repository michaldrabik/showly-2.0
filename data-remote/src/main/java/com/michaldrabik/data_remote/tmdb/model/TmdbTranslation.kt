package com.michaldrabik.data_remote.tmdb.model

data class TmdbTranslation(
  // ex: zh
  val iso_639_1: String,
  // ex: CN
  val iso_3166_1: String,
  val data: Data?,
) {

  data class Data(
    val biography: String?,
  )
}
