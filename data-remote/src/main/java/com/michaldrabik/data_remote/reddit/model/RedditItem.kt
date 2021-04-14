package com.michaldrabik.data_remote.reddit.model

data class RedditItem(
  val id: String,
  val is_self: Boolean,
  val title: String,
  val created: Long,
)
