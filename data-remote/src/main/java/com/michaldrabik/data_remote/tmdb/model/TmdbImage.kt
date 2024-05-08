package com.michaldrabik.data_remote.tmdb.model

import kotlin.math.sqrt

data class TmdbImage(
  val file_path: String,
  val vote_average: Float,
  val vote_count: Long,
  val iso_639_1: String?,
) {

  fun isPlain() = iso_639_1 == null

  fun isEnglish() = iso_639_1 == "en"

  fun isLanguage(language: String) = iso_639_1 == language

  fun getVoteScore(): Double {
    // Calculate the Wilson score interval lower bound: https://en.wikipedia.org/wiki/Binomial_proportion_confidence_interval#Wilson_score_interval
    val z = 1.96 // Z-score corresponding to a 95% confidence level
    val phat = vote_average / 10.0 // Proportion of average rating out of 10
    val numerator = phat + (z * z) / (2 * vote_count) - z * sqrt((phat * (1 - phat) + (z * z) / (4 * vote_count)) / vote_count)
    val denominator = 1 + (z * z) / vote_count
    return if (vote_count > 0) (numerator / denominator) else 0.0
  }
}
