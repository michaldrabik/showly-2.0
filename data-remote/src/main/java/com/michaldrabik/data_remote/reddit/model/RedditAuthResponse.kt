package com.michaldrabik.data_remote.reddit.model

data class RedditAuthResponse(
  val access_token: String,
  val token_type: String,
  val device_id: String,
  val expires_in: Long,
  val scope: String,
)
