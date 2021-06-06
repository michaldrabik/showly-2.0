package com.michaldrabik.repository

import androidx.room.withTransaction
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.CustomListItem
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_model.IdTrakt
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListsRepository @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun createList(
    name: String,
    description: String?,
    idTrakt: Long?,
    idSlug: String?
  ): CustomList {
    val list = CustomList.create().copy(
      idTrakt = idTrakt,
      idSlug = idSlug ?: "",
      name = name.trim(),
      description = description?.trim()
    )
    val listDb = mappers.customList.toDatabase(list)
    database.customListsDao().insert(listOf(listDb))
    return list
  }

  suspend fun updateList(
    id: Long,
    idTrakt: Long?,
    idSlug: String?,
    name: String,
    description: String?
  ): CustomList {
    val listDb = database.customListsDao().getById(id)!!
    val updated = listDb.copy(
      name = name,
      idTrakt = idTrakt ?: listDb.idTrakt,
      idSlug = idSlug ?: listDb.idSlug,
      description = description,
      updatedAt = nowUtcMillis()
    )
    database.customListsDao().update(listOf(updated))
    return mappers.customList.fromDatabase(updated)
  }

  suspend fun deleteList(listId: Long) = database.customListsDao().deleteById(listId)

  suspend fun addToList(listId: Long, itemTraktId: IdTrakt, itemType: String) {
    val timestamp = nowUtcMillis()
    val itemDb = CustomListItem(
      rank = 0,
      idList = listId,
      idTrakt = itemTraktId.id,
      type = itemType,
      listedAt = timestamp,
      createdAt = timestamp,
      updatedAt = timestamp
    )
    database.withTransaction {
      database.customListsItemsDao().insertItem(itemDb)
      database.customListsDao().updateTimestamp(listId, nowUtcMillis())
    }
  }

  suspend fun removeFromList(listId: Long, itemTraktId: IdTrakt, itemType: String) {
    database.withTransaction {
      database.customListsItemsDao().deleteItem(listId, itemTraktId.id, itemType)
      database.customListsDao().updateTimestamp(listId, nowUtcMillis())
    }
  }

  suspend fun loadListIdsForItem(itemTraktId: IdTrakt, itemType: String) =
    database.customListsItemsDao().getListsForItem(itemTraktId.id, itemType)

  suspend fun loadListItemsForId(listId: Long) =
    database.customListsItemsDao().getItemsById(listId)

  suspend fun loadById(listId: Long): CustomList {
    val listDb = database.customListsDao().getById(listId)!!
    return mappers.customList.fromDatabase(listDb)
  }

  suspend fun loadItemsById(listId: Long) =
    database.customListsItemsDao().getItemsById(listId)

  suspend fun loadAll(): List<CustomList> {
    val listsDb = database.customListsDao().getAll()
    return listsDb.map { mappers.customList.fromDatabase(it) }
  }
}
