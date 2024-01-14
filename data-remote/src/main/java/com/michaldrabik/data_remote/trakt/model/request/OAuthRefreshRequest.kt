package com.michaldrabik.data_remote.trakt.model.request

data class OAuthRefreshRequest(
  val refresh_token: String,
  val client_id: String,
  val client_secret: String,
  val redirect_uri: String,
  val grant_type: String = "refresh_token"
)
