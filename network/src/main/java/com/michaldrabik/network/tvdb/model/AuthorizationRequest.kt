package com.michaldrabik.network.tvdb.model

data class AuthorizationRequest(
  val apikey: String,
  val username: String,
  val userkey: String
)