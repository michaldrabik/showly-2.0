package com.michaldrabik.ui_model

import org.threeten.bp.ZonedDateTime

data class Comment(
  val id: Long,
  val parentId: Long,
  val comment: String,
  val userRating: Int,
  val spoiler: Boolean,
  val review: Boolean,
  val likes: Long,
  val replies: Long,
  val createdAt: ZonedDateTime?,
  val updatedAt: ZonedDateTime?,
  val user: User,
  val isMe: Boolean
) {

  fun hasSpoilers() = spoiler || comment.contains("spoiler", true)
}
