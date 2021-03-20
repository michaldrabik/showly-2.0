package com.michaldrabik.ui_repository

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.CustomListItem
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject

@AppScope
class ListsRepository @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val userTraktManager: UserTraktManager
) {

  suspend fun createList(
    name: String,
    description: String?
  ): CustomList {
    val list = CustomList.create().copy(
      name = name.trim(),
      description = description?.trim()
    )
    val listDb = mappers.customList.toDatabase(list)
    database.customListsDao().insert(listOf(listDb))
    return list
  }

  suspend fun updateList(
    id: Long,
    name: String,
    description: String?
  ): CustomList {
    val listDb = database.customListsDao().getById(id)!!
    val updated = listDb.copy(name = name, description = description, updatedAt = nowUtcMillis())
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
    database.customListsItemsDao().insertItem(itemDb)
  }

  suspend fun removeFromList(listId: Long, itemTraktId: IdTrakt, itemType: String) =
    database.customListsItemsDao().deleteItem(listId, itemTraktId.id, itemType)

  suspend fun loadListIdsForItem(itemTraktId: IdTrakt, itemType: String) =
    database.customListsItemsDao().getListsForItem(itemTraktId.id, itemType)

  suspend fun loadById(listId: Long): CustomList {
    val listDb = database.customListsDao().getById(listId)!!
    return mappers.customList.fromDatabase(listDb)
  }

  suspend fun loadAll(): List<CustomList> {
    val listsDb = database.customListsDao().getAll()
    return listsDb.map { mappers.customList.fromDatabase(it) }
  }
}
