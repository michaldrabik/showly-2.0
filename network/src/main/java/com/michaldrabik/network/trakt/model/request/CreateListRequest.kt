package com.michaldrabik.network.trakt.model.request

data class CreateListRequest(
  val name: String,
  val description: String?,
)
