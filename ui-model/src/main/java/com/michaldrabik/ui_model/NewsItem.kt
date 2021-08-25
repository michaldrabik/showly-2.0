package com.michaldrabik.ui_model

import java.time.ZonedDateTime

data class NewsItem(
  val id: String,
  val title: String,
  val url: String,
  val type: Type,
  val image: String?,
  val score: Long,
  val datedAt: ZonedDateTime,
  val createdAt: ZonedDateTime,
  val updatedAt: ZonedDateTime
) {

  enum class Type(val slug: String) {
    SHOW("show"),
    MOVIE("movie");

    companion object {
      fun fromSlug(slug: String) = Type.values().first { it.slug == slug }
    }
  }

  val isVideo =
    url.startsWith("https://www.youtu") ||
      url.startsWith("https://youtu") ||
      url.startsWith("www.youtu")

  val isWebLink =
    url.startsWith("http") ||
      url.startsWith("www")
}
