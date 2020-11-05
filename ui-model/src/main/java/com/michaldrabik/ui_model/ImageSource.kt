package com.michaldrabik.ui_model

enum class ImageSource(
  val key: String
) {
  TVDB("tvdb"),
  AWS("aws");

  companion object {
    fun fromKey(key: String) = values().first { it.key == key }
  }
}
