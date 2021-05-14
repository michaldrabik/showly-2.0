package com.michaldrabik.ui_model

data class Ratings(
  val imdb: Value? = null,
  val trakt: Value? = null,
  val rottenTomatoes: Value? = null,
  val metascore: Value? = null,
) {

  data class Value(
    val value: String?,
    val isLoading: Boolean,
  )
}
