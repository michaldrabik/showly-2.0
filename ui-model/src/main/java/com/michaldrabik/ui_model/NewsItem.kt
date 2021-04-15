package com.michaldrabik.ui_model

import org.threeten.bp.ZonedDateTime

data class NewsItem(
  val id: String,
  val title: String,
  val url: String,
  val type: Type,
  val image: String?,
  val score: Long,
  val createdAt: ZonedDateTime,
) {

  enum class Type(val slug: String) {
    SHOW("show"),
    MOVIE("movie")
  }

  val isVideo =
    url.startsWith("https://www.youtu") ||
      url.startsWith("https://youtu") ||
      url.startsWith("www.youtu")
}

