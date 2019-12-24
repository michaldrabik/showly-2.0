package com.michaldrabik.showly2.model

data class Actor(
  val id: Long,
  val tvdbShowId: Long,
  val name: String,
  val role: String,
  val sortOrder: Int,
  val image: String
)
