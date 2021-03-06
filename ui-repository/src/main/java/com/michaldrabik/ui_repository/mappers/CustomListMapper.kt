package com.michaldrabik.ui_repository.mappers

import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.ui_model.CustomList
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject
import com.michaldrabik.storage.database.model.CustomList as CustomListDb

class CustomListMapper @Inject constructor() {

  fun fromDatabase(list: CustomListDb) = CustomList(
    id = list.id,
    idTrakt = list.idTrakt,
    idSlug = list.idSlug,
    name = list.name,
    description = list.description,
    privacy = list.privacy,
    displayNumbers = list.displayNumbers,
    allowComments = list.allowComments,
    sortBy = list.sortBy,
    sortHow = list.sortHow,
    itemCount = list.itemCount,
    commentCount = list.commentCount,
    likes = list.likes,
    createdAt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(list.createdAt), ZoneId.of("UTC")),
    updatedAt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(list.updatedAt), ZoneId.of("UTC"))
  )

  fun toDatabase(list: CustomList) = CustomListDb(
    id = list.id,
    idTrakt = list.idTrakt,
    idSlug = list.idSlug,
    name = list.name,
    description = list.description,
    privacy = list.privacy,
    displayNumbers = list.displayNumbers,
    allowComments = list.allowComments,
    sortBy = list.sortBy,
    sortHow = list.sortHow,
    itemCount = list.itemCount,
    commentCount = list.commentCount,
    likes = list.likes,
    createdAt = list.createdAt.toMillis(),
    updatedAt = list.updatedAt.toMillis()
  )
}
