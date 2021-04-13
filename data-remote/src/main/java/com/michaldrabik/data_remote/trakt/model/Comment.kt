package com.michaldrabik.data_remote.trakt.model

data class Comment(
  val id: Long?,
  val parent_id: Long?,
  val comment: String?,
  val user_rating: Int?,
  val spoiler: Boolean?,
  val review: Boolean?,
  val likes: Long?,
  val replies: Long?,
  val created_at: String?,
  val updated_at: String?,
  val user: TraktUser?
)
