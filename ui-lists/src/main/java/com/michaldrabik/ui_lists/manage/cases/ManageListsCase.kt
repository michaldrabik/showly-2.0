package com.michaldrabik.ui_lists.manage.cases

import com.michaldrabik.common.Mode
import com.michaldrabik.repository.ListsRepository
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_lists.manage.recycler.ManageListsItem
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class ManageListsCase @Inject constructor(
  private val listsRepository: ListsRepository,
  private val quickSyncManager: QuickSyncManager,
) {

  suspend fun loadLists(itemId: IdTrakt, itemType: String) = coroutineScope {
    val listsAsync = async { listsRepository.loadAll() }
    val listsWithItemAsync = async { listsRepository.loadListIdsForItem(itemId, itemType) }
    val (lists, listsWithItem) = Pair(listsAsync.await(), listsWithItemAsync.await())
    lists
      .sortedBy { it.name }
      .map {
        val isChecked = listsWithItem.contains(it.id)
        ManageListsItem(it, isChecked, true)
      }
  }

  suspend fun addToList(
    itemId: IdTrakt,
    itemType: String,
    listItem: ManageListsItem,
  ) {
    listsRepository.addToList(listItem.list.id, itemId, itemType)
    quickSyncManager.scheduleAddToList(itemId.id, listItem.list.id, Mode.fromType(itemType))
  }

  suspend fun removeFromList(
    itemId: IdTrakt,
    itemType: String,
    listItem: ManageListsItem,
  ) {
    listsRepository.removeFromList(listItem.list.id, itemId, itemType)
    quickSyncManager.scheduleRemoveFromList(itemId.id, listItem.list.id, Mode.fromType(itemType))
  }
}
