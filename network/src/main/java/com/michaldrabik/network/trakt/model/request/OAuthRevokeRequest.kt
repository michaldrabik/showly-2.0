package com.michaldrabik.network.trakt.model.request

data class OAuthRevokeRequest(
  val token: String,
  val client_id: String,
  val client_secret: String
)
