package com.michaldrabik.network.tmdb.model

data class TmdbImage(
  val file_path: String,
  val vote_average: Float,
  val vote_count: Long,
  val iso_639_1: String?
) {

  fun isEnglish() = iso_639_1 == "en"

}
