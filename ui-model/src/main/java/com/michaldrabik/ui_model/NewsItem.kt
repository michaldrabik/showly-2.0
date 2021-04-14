package com.michaldrabik.ui_model

import org.threeten.bp.ZonedDateTime

data class NewsItem(
  val id: String,
  val title: String,
  val type: Type,
  val createdAt: ZonedDateTime,
) {

  enum class Type(val slug: String) {
    SHOW("show"),
    MOVIE("movie")
  }
}

