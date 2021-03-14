package com.michaldrabik.ui_repository

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_model.CustomList
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

  suspend fun deleteList(listId: Long) = database.customListsDao().deleteById(listId)

  suspend fun loadAll(): List<CustomList> {
    val listsDb = database.customListsDao().getAll()
    return listsDb.map { mappers.customList.fromDatabase(it) }
  }
}
