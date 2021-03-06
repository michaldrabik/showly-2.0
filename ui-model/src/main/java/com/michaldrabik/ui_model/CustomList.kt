package com.michaldrabik.ui_model

import org.threeten.bp.ZonedDateTime

data class CustomList(
  val id: Long,
  val idTrakt: Long?,
  val idSlug: String,
  val name: String,
  val description: String?,
  val privacy: String,
  val displayNumbers: Boolean,
  val allowComments: Boolean,
  val sortBy: String,
  val sortHow: String,
  val itemCount: Long,
  val commentCount: Long,
  val likes: Long,
  val createdAt: ZonedDateTime,
  val updatedAt: ZonedDateTime
)
