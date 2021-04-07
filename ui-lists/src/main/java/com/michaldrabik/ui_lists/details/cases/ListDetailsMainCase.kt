package com.michaldrabik.ui_lists.details.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.CustomListItem
import com.michaldrabik.ui_base.utilities.extensions.runTransaction
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import com.michaldrabik.ui_model.CustomList
import com.michaldrabik.ui_repository.ListsRepository
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.UserTraktManager
import retrofit2.HttpException
import javax.inject.Inject

@AppScope
class ListDetailsMainCase @Inject constructor(
  private val database: AppDatabase,
  private val cloud: Cloud,
  private val listsRepository: ListsRepository,
  private val settingsRepository: SettingsRepository,
  private val userTraktManager: UserTraktManager,
) {

  suspend fun loadDetails(id: Long) = listsRepository.loadById(id)

  suspend fun updateRanks(listId: Long, items: List<ListDetailsItem>): List<ListDetailsItem> {
    val now = nowUtcMillis()
    val listItems = listsRepository.loadItemsById(listId)
    val updateItems = mutableListOf<ListDetailsItem>()
    val updateItemsDb = mutableListOf<CustomListItem>()
    items.forEachIndexed { index, item ->
      val dbItem = listItems.first { it.id == item.id }.copy(rank = index + 1L, updatedAt = now)
      val updatedItem = item.copy(rank = index + 1L)
      updateItems.add(updatedItem)
      updateItemsDb.add(dbItem)
    }
    database.runTransaction {
      customListsItemsDao().update(updateItemsDb)
      customListsDao().updateTimestamp(listId, now)
    }
    return updateItems
  }

  suspend fun deleteList(listId: Long, removeFromTrakt: Boolean) {
    val isAuthorized = userTraktManager.isAuthorized()
    val list = listsRepository.loadById(listId)
    val listIdTrakt = list.idTrakt
    if (isAuthorized && removeFromTrakt && listIdTrakt != null) {
      val token = userTraktManager.checkAuthorization()
      try {
        cloud.traktApi.deleteList(token.token, listIdTrakt)
      } catch (error: Throwable) {
        if (error is HttpException && error.code() == 404) {
          // NOOP List does not exist in Trakt already.
        } else {
          throw error
        }
      }
    }
    listsRepository.deleteList(listId)
  }

  suspend fun isQuickRemoveEnabled(list: CustomList): Boolean {
    return list.idTrakt != null && settingsRepository.load().traktQuickRemoveEnabled
  }
}
