package com.michaldrabik.network.trakt.model.json

data class UserImageJson(
  val avatar: UserAvatarImageJson?
)

data class UserAvatarImageJson(
  val full: String?
)