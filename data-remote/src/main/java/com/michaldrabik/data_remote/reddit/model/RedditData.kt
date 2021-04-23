package com.michaldrabik.data_remote.reddit.model

data class RedditData(
  val children: List<RedditDataItem>,
  val after: String?,
  val before: String?,
)
