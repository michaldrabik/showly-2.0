package com.michaldrabik.data_remote.trakt.model

data class CustomList(
  val ids: Ids,
  val name: String,
  val description: String?,
  val privacy: String,
  val display_numbers: Boolean,
  val allow_comments: Boolean,
  val sort_by: String,
  val sort_how: String,
  val item_count: Long,
  val comment_count: Long,
  val likes: Long,
  val created_at: String,
  val updated_at: String
) {

  data class Ids(
    val trakt: Long,
    val slug: String
  )
}
