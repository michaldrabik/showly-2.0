package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.network.trakt.model.User
import com.michaldrabik.showly2.model.Comment
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject
import com.michaldrabik.network.trakt.model.Comment as CommentNetwork

class CommentMapper @Inject constructor() {

  fun fromNetwork(comment: CommentNetwork?) =
    Comment(
      id = comment?.id ?: -1,
      parentId = comment?.parent_id ?: -1,
      comment = comment?.comment ?: "",
      userRating = comment?.user_rating ?: -1,
      spoiler = comment?.spoiler ?: false,
      review = comment?.review ?: false,
      createdAt = if (comment?.created_at.isNullOrBlank()) null else ZonedDateTime.parse(comment?.created_at),
      user = User(
        username = comment?.user?.username ?: "",
        avatarUrl = comment?.user?.avatarUrl ?: ""
      )
    )
}
