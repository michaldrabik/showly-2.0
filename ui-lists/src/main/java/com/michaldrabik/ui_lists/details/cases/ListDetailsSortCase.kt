package com.michaldrabik.ui_lists.details.cases

import com.michaldrabik.common.Mode
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ListDetailsSortCase @Inject constructor(
  private val localSource: LocalDataSource,
  private val mappers: Mappers
) {

  suspend fun setSortOrder(listId: Long, sortOrder: SortOrder, sortType: SortType): CustomList {
    localSource.customLists.updateSortByLocal(
      listId,
      sortOrder.slug,
      sortType.slug,
      nowUtcMillis()
    )
    val list = localSource.customLists.getById(listId)!!
    return mappers.customList.fromDatabase(list)
  }

  suspend fun setFilterTypes(listId: Long, types: List<Mode>): CustomList {
    localSource.customLists.updateFilterTypeLocal(
      listId,
      types.joinToString(",") { it.type },
      nowUtcMillis()
    )
    val list = localSource.customLists.getById(listId)!!
    return mappers.customList.fromDatabase(list)
  }
}
