package com.michaldrabik.data_remote.tmdb.model

data class TmdbTranslation(
  val iso_639_1: String, // ex: zh
  val iso_3166_1: String, // ex: CN
  val data: Data?
) {

  data class Data(
    val biography: String?
  )
}
