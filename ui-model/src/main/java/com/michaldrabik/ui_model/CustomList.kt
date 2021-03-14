package com.michaldrabik.ui_model

import com.michaldrabik.common.extensions.nowUtc
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
  val sortBy: SortOrderList,
  val sortHow: SortType,
  val itemCount: Long,
  val commentCount: Long,
  val likes: Long,
  val createdAt: ZonedDateTime,
  val updatedAt: ZonedDateTime
) {

  companion object {
    fun create() = CustomList(
      id = 0,
      idTrakt = null,
      idSlug = "",
      name = "",
      description = null,
      privacy = "private",
      displayNumbers = true,
      allowComments = false,
      sortBy = SortOrderList.RANK,
      sortHow = SortType.ASCENDING,
      itemCount = 0,
      commentCount = 0,
      likes = 0,
      createdAt = nowUtc(),
      updatedAt = nowUtc()
    )
  }
}
