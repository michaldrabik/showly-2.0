package com.michaldrabik.data_remote.trakt.model.request

data class OAuthRevokeRequest(
  val token: String,
  val client_id: String,
  val client_secret: String
)
