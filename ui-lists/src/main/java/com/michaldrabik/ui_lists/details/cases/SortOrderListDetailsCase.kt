package com.michaldrabik.ui_lists.details.cases

import androidx.room.withTransaction
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_model.SortOrderList
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject

@AppScope
class SortOrderListDetailsCase @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun setSortOrder(listId: Long, sortOrder: SortOrderList): CustomList {
    database.withTransaction {
      database.customListsDao().updateSortByLocal(listId, sortOrder.slug)
      database.customListsDao().updateTimestamp(listId, nowUtcMillis())
    }
    val list = database.customListsDao().getById(listId)!!
    return mappers.customList.fromDatabase(list)
  }
}
