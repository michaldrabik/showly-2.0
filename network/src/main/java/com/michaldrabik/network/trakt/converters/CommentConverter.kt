package com.michaldrabik.network.trakt.converters

import com.michaldrabik.network.trakt.model.Comment
import com.michaldrabik.network.trakt.model.json.CommentJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.threeten.bp.ZonedDateTime

class CommentConverter(
  private val userConverter: UserConverter
) {

  @FromJson
  fun fromJson(json: CommentJson) =
    Comment(
      id = json.id ?: -1,
      parentId = json.parent_id ?: -1,
      comment = json.comment ?: "",
      userRating = json.user_rating ?: -1,
      spoiler = json.spoiler ?: true,
      review = json.spoiler ?: false,
      createdAt = if (json.created_at.isNullOrBlank()) null else ZonedDateTime.parse(json.created_at),
      user = userConverter.fromJson(json.user)
    )

  @ToJson
  fun toJson(value: Comment): CommentJson = throw UnsupportedOperationException()
}