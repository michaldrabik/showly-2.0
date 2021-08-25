package com.michaldrabik.ui_model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.ZonedDateTime

@Parcelize
data class Comment(
  val id: Long,
  val parentId: Long,
  val comment: String,
  val userRating: Int,
  val spoiler: Boolean,
  val review: Boolean,
  val likes: Long,
  val replies: Long,
  val createdAt: ZonedDateTime?,
  val updatedAt: ZonedDateTime?,
  val user: User,
  val isMe: Boolean,
  val isSignedIn: Boolean,
  val isLoading: Boolean,
  val hasRepliesLoaded: Boolean
) : Parcelable {

  fun hasSpoilers() = spoiler || comment.contains("spoiler", true)

  fun isReply() = parentId > 0

  fun getReplyId() = if (isReply()) parentId else id
}
