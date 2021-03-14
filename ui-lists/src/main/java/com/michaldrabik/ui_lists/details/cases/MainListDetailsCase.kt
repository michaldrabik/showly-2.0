package com.michaldrabik.ui_lists.details.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_lists.lists.recycler.ListsItem
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_repository.ListsRepository
import com.michaldrabik.ui_repository.SettingsRepository
import javax.inject.Inject

@AppScope
class MainListDetailsCase @Inject constructor(
  private val listsRepository: ListsRepository,
  private val dateProvider: DateFormatProvider,
  private val settingsRepository: SettingsRepository
) {

  suspend fun loadItems(): List<ListsItem> {
    val lists = listsRepository.loadAll()
    val dateFormat = dateProvider.loadFullDayFormat()
    val sortType = settingsRepository.load().listsSortBy

    return lists
      .sortedByType(sortType)
      .map { ListsItem(it, sortType, dateFormat) }
  }

  suspend fun deleteList(listId: Long) {
    listsRepository.deleteList(listId)
  }

  private fun List<CustomList>.sortedByType(sortType: SortOrder) =
    when (sortType) {
      SortOrder.NAME -> this.sortedBy { it.name }
      SortOrder.NEWEST -> this.sortedByDescending { it.createdAt }
      SortOrder.DATE_UPDATED -> this.sortedByDescending { it.updatedAt }
      else -> error("Should not be used here.")
    }
}
