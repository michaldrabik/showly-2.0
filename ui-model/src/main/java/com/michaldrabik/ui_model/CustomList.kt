package com.michaldrabik.ui_model

import android.os.Parcelable
import com.michaldrabik.common.Mode
import com.michaldrabik.common.extensions.nowUtc
import kotlinx.android.parcel.Parcelize
import java.time.ZonedDateTime

@Parcelize
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
  val sortByLocal: SortOrderList,
  val sortHowLocal: SortType,
  val filterTypeLocal: List<Mode>,
  val itemCount: Long,
  val commentCount: Long,
  val likes: Long,
  val createdAt: ZonedDateTime,
  val updatedAt: ZonedDateTime
) : Parcelable {

  companion object {
    fun create() = CustomList(
      id = 0,
      idTrakt = null,
      idSlug = "",
      name = "",
      description = null,
      privacy = "private",
      displayNumbers = false,
      allowComments = true,
      sortBy = SortOrderList.RANK,
      sortHow = SortType.ASCENDING,
      sortByLocal = SortOrderList.RANK,
      sortHowLocal = SortType.ASCENDING,
      filterTypeLocal = Mode.getAll(),
      itemCount = 0,
      commentCount = 0,
      likes = 0,
      createdAt = nowUtc(),
      updatedAt = nowUtc()
    )
  }
}
