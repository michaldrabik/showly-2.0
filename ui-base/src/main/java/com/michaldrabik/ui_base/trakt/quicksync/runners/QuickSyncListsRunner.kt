package com.michaldrabik.ui_base.trakt.quicksync.runners

import com.michaldrabik.common.Mode
import com.michaldrabik.common.errors.ErrorHelper
import com.michaldrabik.common.errors.ShowlyError
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.TraktSyncQueue
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Operation
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Type
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.repository.ListsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import com.michaldrabik.ui_model.CustomList
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuickSyncListsRunner @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val listsRepository: ListsRepository,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  companion object {
    private const val TRAKT_DELAY = 1200L
  }

  private val syncTypes = listOf(
    Type.LIST_ITEM_SHOW,
    Type.LIST_ITEM_MOVIE
  ).map { it.slug }

  override suspend fun run(): Int {
    Timber.d("Initialized.")

    var count = 0
    checkAuthorization()

    val items = localSource.traktSyncQueue.getAll(syncTypes)
      .groupBy { it.idList }
      .filter { it.key != null }

    if (items.isEmpty()) {
      Timber.d("Nothing to sync. Cancelling..")
      return count
    }

    count += processItems(items, count)

    Timber.d("Finished with success.")
    return count
  }

  private suspend fun processItems(
    items: Map<Long?, List<TraktSyncQueue>>,
    count: Int
  ): Int {
    var counted = count

    for (syncListItem in items) {
      val listId = syncListItem.key!!
      var localList = localSource.customLists.getById(listId)?.run {
        mappers.customList.fromDatabase(this)
      }
      if (localList == null) {
        localSource.traktSyncQueue.deleteAllForList(listId)
        Timber.d("List with ID: $listId does not exist anymore. Skipping...")
        continue
      }

      val addItems = syncListItem.value.filter { it.operation == Operation.ADD.slug }
      val removeItems = syncListItem.value.filter { it.operation == Operation.REMOVE.slug }

      if (localList.idTrakt == null && addItems.isNotEmpty()) {
        Timber.d("List with ID: $listId does not exist in Trakt. Creating...")
        localList = createMissingList(localList, addItems)
      } else if (localList.idTrakt == null) {
        Timber.d("List with ID: $listId does not exist in Trakt. No need to remove items...")
        localSource.traktSyncQueue.delete(removeItems)
        continue
      }

      // Handle remove items operation
      handleRemoveItems(removeItems, localList)

      // Handle add items operation
      handleAddItems(addItems, localList)

      counted++
      delay(TRAKT_DELAY)
    }

    // Check in case more items appeared in the meantime.
    val itemsCheck = localSource.traktSyncQueue.getAll(syncTypes)
      .groupBy { it.idList }
      .filter { it.key != null }

    if (itemsCheck.isNotEmpty()) {
      return processItems(itemsCheck, counted)
    }

    return counted
  }

  private suspend fun handleRemoveItems(
    removeItems: List<TraktSyncQueue>,
    list: CustomList
  ) {
    try {
      val showIds = removeItems
        .filter { it.type == Type.LIST_ITEM_SHOW.slug }
        .map { it.idTrakt }

      val movieIds = removeItems
        .filter { it.type == Type.LIST_ITEM_MOVIE.slug }
        .map { it.idTrakt }

      if (showIds.isNotEmpty() || movieIds.isNotEmpty()) {
        remoteSource.trakt.postRemoveListItems(list.idTrakt!!, showIds, movieIds)
        localSource.traktSyncQueue.delete(removeItems)
      }
    } catch (error: Throwable) {
      when (ErrorHelper.parse(error)) {
        is ShowlyError.ResourceNotFoundError -> {
          localSource.traktSyncQueue.delete(removeItems)
          Timber.d("Tried to remove from list but it does not exist anymore. Skipping...")
        }
        else -> throw error
      }
    }
  }

  private suspend fun handleAddItems(
    addItems: List<TraktSyncQueue>,
    localList: CustomList
  ) {
    val showIds = addItems
      .filter { it.type == Type.LIST_ITEM_SHOW.slug }
      .map { it.idTrakt }

    val movieIds = addItems
      .filter { it.type == Type.LIST_ITEM_MOVIE.slug }
      .map { it.idTrakt }

    try {
      if (showIds.isNotEmpty() || movieIds.isNotEmpty()) {
        remoteSource.trakt.postAddListItems(localList.idTrakt!!, showIds, movieIds)
        localSource.traktSyncQueue.delete(addItems)
      }
    } catch (error: Throwable) {
      when (ErrorHelper.parse(error)) {
        is ShowlyError.AccountLimitsError -> {
          Timber.d("Account limits for lists reached.")
          localSource.traktSyncQueue.delete(addItems)
          throw error
        }
        is ShowlyError.ResourceNotFoundError -> {
          Timber.d("Tried to add to list but it does not exist. Creating...")
          delay(TRAKT_DELAY)
          createMissingList(localList, addItems)
          localSource.traktSyncQueue.delete(addItems)
        }
        else -> throw error
      }
    }
  }

  private suspend fun createMissingList(
    localList: CustomList,
    addItems: List<TraktSyncQueue>
  ): CustomList {
    try {
      val result = remoteSource.trakt.postCreateList(localList.name, localList.description)
        .run { mappers.customList.fromNetwork(this) }

      listsRepository.updateList(localList.id, result.idTrakt, result.idSlug, result.name, result.description)

      val localItems = listsRepository.loadListItemsForId(localList.id)
      if (localItems.isNotEmpty()) {
        val showsIds = localItems.filter { it.type == Mode.SHOWS.type }.map { it.idTrakt }
        val moviesIds = localItems.filter { it.type == Mode.MOVIES.type }.map { it.idTrakt }
        delay(TRAKT_DELAY)
        remoteSource.trakt.postAddListItems(result.idTrakt!!, showsIds, moviesIds)
      }

      return listsRepository.updateList(localList.id, result.idTrakt, result.idSlug, result.name, result.description)
    } catch (error: Throwable) {
      when (ErrorHelper.parse(error)) {
        ShowlyError.AccountLimitsError -> {
          localSource.traktSyncQueue.delete(addItems)
          throw error
        }
        else -> throw error
      }
    }
  }
}
