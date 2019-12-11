package com.michaldrabik.network.trakt.model

data class Comment(
  val id: Long,
  val parentId: Long,
  val comment: String,
  val userRating: Int,
  val spoiler: Boolean,
  val review: Boolean
)