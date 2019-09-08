package com.michaldrabik.showly2.model

import org.threeten.bp.OffsetDateTime

data class RecentSearch(
  val text: String,
  val createdAt: OffsetDateTime
)
