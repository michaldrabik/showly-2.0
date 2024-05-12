package com.michaldrabik.ui_model

enum class ImageSource(
  val key: String,
) {
  TMDB("tmdb"),
  CUSTOM("custom"),
  AWS("aws");

  companion object {
    fun fromKey(key: String) = entries.first { it.key == key }
  }
}
