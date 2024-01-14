package com.michaldrabik.common

enum class Mode(val type: String) {
  SHOWS("show"),
  MOVIES("movie");

  companion object {
    fun fromType(type: String) = values().first { it.type == type }

    fun getAll() = listOf(SHOWS, MOVIES)
  }
}
