package com.michaldrabik.network.trakt.model

import org.threeten.bp.ZonedDateTime

data class Comment(
  val id: Long,
  val parentId: Long,
  val comment: String,
  val userRating: Int,
  val spoiler: Boolean,
  val review: Boolean,
  val createdAt: ZonedDateTime?,
  val user: User
) {

  fun isSpoiler() = spoiler || comment.contains("spoiler")
}
