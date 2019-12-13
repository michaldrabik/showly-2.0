package com.michaldrabik.network.trakt.converters

import com.michaldrabik.network.trakt.model.User
import com.michaldrabik.network.trakt.model.json.UserJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class UserConverter {

  @FromJson
  fun fromJson(json: UserJson?) =
    User(
      username = json?.username ?: "",
      avatarUrl = json?.images?.avatar?.full ?: ""
    )

  @ToJson
  fun toJson(value: User): UserJson = throw UnsupportedOperationException()
}