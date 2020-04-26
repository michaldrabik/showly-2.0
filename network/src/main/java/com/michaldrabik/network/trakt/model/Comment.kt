package com.michaldrabik.network.trakt.model

data class Comment(
  val id: Long?,
  val parent_id: Long?,
  val comment: String?,
  val user_rating: Int?,
  val spoiler: Boolean?,
  val review: Boolean?,
  val created_at: String?,
  val user: User?
)
