package com.michaldrabik.ui_lists.details.cases

import com.michaldrabik.common.errors.ErrorHelper
import com.michaldrabik.common.errors.ShowlyError
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.CustomListItem
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.repository.ListsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_lists.details.recycler.ListDetailsItem
import com.michaldrabik.ui_model.CustomList
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ListDetailsMainCase @Inject constructor(
  private val localSource: LocalDataSource,
  private val remoteSource: RemoteDataSource,
  private val transactions: TransactionsProvider,
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
    transactions.withTransaction {
      localSource.customListsItems.update(updateItemsDb)
      localSource.customLists.updateTimestamp(listId, now)
    }
    return updateItems
  }

  suspend fun deleteList(listId: Long, removeFromTrakt: Boolean) {
    val isAuthorized = userTraktManager.isAuthorized()
    val isQuickRemove = settingsRepository.load().traktQuickRemoveEnabled
    val list = listsRepository.loadById(listId)
    val listIdTrakt = list.idTrakt

    if (isQuickRemove && isAuthorized && removeFromTrakt && listIdTrakt != null) {
      userTraktManager.checkAuthorization()
      try {
        remoteSource.trakt.deleteList(listIdTrakt)
      } catch (error: Throwable) {
        when (ErrorHelper.parse(error)) {
          is ShowlyError.ResourceNotFoundError -> Unit // NOOP List does not exist in Trakt.
          else -> throw error
        }
      }
    }

    listsRepository.deleteList(listId)
  }

  suspend fun isQuickRemoveEnabled(list: CustomList) =
    list.idTrakt != null && settingsRepository.load().traktQuickRemoveEnabled
}
