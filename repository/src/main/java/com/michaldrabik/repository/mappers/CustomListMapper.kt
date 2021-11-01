package com.michaldrabik.repository.mappers

import com.michaldrabik.common.Mode
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import com.michaldrabik.data_local.database.model.CustomList as CustomListDb
import com.michaldrabik.data_remote.trakt.model.CustomList as CustomListNetwork
import com.michaldrabik.data_remote.trakt.model.CustomList.Ids as IdsList

class CustomListMapper @Inject constructor() {

  fun fromNetwork(list: CustomListNetwork) = CustomList(
    id = 0,
    idTrakt = list.ids.trakt,
    idSlug = list.ids.slug,
    name = list.name,
    description = list.description,
    privacy = list.privacy,
    displayNumbers = list.display_numbers,
    allowComments = list.allow_comments,
    sortBy = SortOrder.fromSlug(list.sort_by) ?: SortOrder.RANK,
    sortHow = SortType.fromSlug(list.sort_how),
    sortByLocal = SortOrder.RANK,
    sortHowLocal = SortType.ASCENDING,
    filterTypeLocal = Mode.getAll(),
    itemCount = list.item_count,
    commentCount = list.comment_count,
    likes = list.likes,
    createdAt = ZonedDateTime.parse(list.created_at),
    updatedAt = ZonedDateTime.parse(list.updated_at)
  )

  fun fromDatabase(list: CustomListDb) = CustomList(
    id = list.id,
    idTrakt = list.idTrakt,
    idSlug = list.idSlug,
    name = list.name,
    description = list.description,
    privacy = list.privacy,
    displayNumbers = list.displayNumbers,
    allowComments = list.allowComments,
    sortBy = SortOrder.fromSlug(list.sortBy) ?: SortOrder.RANK,
    sortHow = SortType.fromSlug(list.sortHow),
    sortByLocal = SortOrder.fromSlug(list.sortByLocal) ?: SortOrder.RANK,
    sortHowLocal = SortType.fromSlug(list.sortHowLocal),
    filterTypeLocal = when {
      list.filterTypeLocal.isEmpty() -> emptyList()
      else -> list.filterTypeLocal.split(",").map { Mode.fromType(it) }
    },
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
    sortBy = list.sortBy.slug,
    sortHow = list.sortHow.slug,
    sortByLocal = list.sortByLocal.slug,
    sortHowLocal = list.sortHowLocal.slug,
    filterTypeLocal = list.filterTypeLocal.joinToString(",") { it.type },
    itemCount = list.itemCount,
    commentCount = list.commentCount,
    likes = list.likes,
    createdAt = list.createdAt.toMillis(),
    updatedAt = list.updatedAt.toMillis()
  )

  fun toNetwork(list: CustomList) = CustomListNetwork(
    ids = IdsList(
      trakt = list.idTrakt ?: -1,
      slug = list.idSlug
    ),
    name = list.name,
    description = list.description,
    privacy = list.privacy,
    display_numbers = list.displayNumbers,
    allow_comments = list.allowComments,
    sort_by = list.sortBy.slug,
    sort_how = list.sortHow.slug,
    item_count = list.itemCount,
    comment_count = list.commentCount,
    likes = list.likes,
    created_at = list.createdAt.format(DateTimeFormatter.ISO_INSTANT),
    updated_at = list.updatedAt.format(DateTimeFormatter.ISO_INSTANT)
  )
}
