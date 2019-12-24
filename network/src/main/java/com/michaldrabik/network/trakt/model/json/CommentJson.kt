package com.michaldrabik.network.trakt.model.json

data class CommentJson(
  val id: Long?,
  val parent_id: Long?,
  val comment: String?,
  val user_rating: Int?,
  val spoiler: Boolean?,
  val review: Boolean?,
  val created_at: String?,
  val user: UserJson?
)
