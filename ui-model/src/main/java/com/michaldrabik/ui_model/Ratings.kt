package com.michaldrabik.ui_model

data class Ratings(
  val trakt: Value? = null,
  val imdb: Value? = null,
  val metascore: Value? = null,
  val rottenTomatoes: Value? = null,
  val rottenTomatoesUrl: String? = null,
) {

  fun isAnyLoading() =
    trakt?.isLoading == true ||
      imdb?.isLoading == true ||
      metascore?.isLoading == true ||
      rottenTomatoes?.isLoading == true

  data class Value(
    val value: String?,
    val isLoading: Boolean,
  )
}
