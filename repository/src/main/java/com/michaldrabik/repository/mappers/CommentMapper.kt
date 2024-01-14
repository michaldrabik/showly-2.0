package com.michaldrabik.repository.mappers

import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.User
import java.time.ZonedDateTime
import javax.inject.Inject
import com.michaldrabik.data_remote.trakt.model.Comment as CommentNetwork

class CommentMapper @Inject constructor() {

  fun fromNetwork(comment: CommentNetwork?) =
    Comment(
      id = comment?.id ?: -1,
      parentId = comment?.parent_id ?: -1,
      comment = comment?.comment ?: "",
      userRating = comment?.user_rating ?: -1,
      spoiler = comment?.spoiler ?: false,
      review = comment?.review ?: false,
      likes = comment?.likes ?: 0,
      replies = comment?.replies ?: 0,
      createdAt = if (comment?.created_at.isNullOrBlank()) null else ZonedDateTime.parse(comment?.created_at),
      updatedAt = if (comment?.updated_at.isNullOrBlank()) null else ZonedDateTime.parse(comment?.updated_at),
      user = User(
        username = comment?.user?.username ?: "",
        avatarUrl = comment?.user?.images?.avatar?.full ?: ""
      ),
      isMe = false,
      isSignedIn = false,
      isLoading = false,
      hasRepliesLoaded = false
    )
}
