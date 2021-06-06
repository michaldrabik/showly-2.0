package com.michaldrabik.ui_base.trakt.quicksync.runners

import com.michaldrabik.common.Mode
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.TraktSyncQueue
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Operation
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Type
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.ListsRepository
import com.michaldrabik.repository.TraktAuthToken
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import com.michaldrabik.ui_model.CustomList
import kotlinx.coroutines.delay
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuickSyncListsRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val listsRepository: ListsRepository,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  companion object {
    private const val TRAKT_DELAY = 1000L
  }

  private val syncTypes = listOf(Type.LIST_ITEM_SHOW, Type.LIST_ITEM_MOVIE).map { it.slug }

  override suspend fun run(): Int {
    Timber.d("Initialized.")
    isRunning = true

    var count = 0
    val authToken = checkAuthorization()

    val items = database.traktSyncQueueDao().getAll(syncTypes)
      .groupBy { it.idList }
      .filter { it.key != null }

    if (items.isEmpty()) {
      Timber.d("Nothing to sync. Cancelling..")
      return count
    }

    count += processItems(items, authToken, count)

    isRunning = false
    Timber.d("Finished with success.")
    return count
  }

  private suspend fun processItems(
    items: Map<Long?, List<TraktSyncQueue>>,
    authToken: TraktAuthToken,
    count: Int
  ): Int {
    var counted = count

    for (syncListItem in items) {
      val listId = syncListItem.key!!
      var localList = database.customListsDao().getById(listId)?.run {
        mappers.customList.fromDatabase(this)
      }
      if (localList == null) {
        database.traktSyncQueueDao().deleteAllForList(listId)
        Timber.d("List with ID: $listId does not exist anymore. Skipping...")
        continue
      }

      val addItems = syncListItem.value.filter { it.operation == Operation.ADD.slug }
      val removeItems = syncListItem.value.filter { it.operation == Operation.REMOVE.slug }

      if (localList.idTrakt == null && addItems.isNotEmpty()) {
        Timber.d("List with ID: $listId does not exist in Trakt. Creating...")
        localList = createMissingList(localList, authToken)
      } else if (localList.idTrakt == null) {
        Timber.d("List with ID: $listId does not exist in Trakt. No need to remove items...")
        database.traktSyncQueueDao().delete(removeItems)
        continue
      }

      // Handle remove items operation
      handleRemoveItems(removeItems, authToken, localList)

      // Handle add items operation
      handleAddItems(addItems, authToken, localList)

      counted++
      delay(TRAKT_DELAY)
    }

    // Check in case more items appeared in the meantime.
    val itemsCheck = database.traktSyncQueueDao().getAll(syncTypes)
      .groupBy { it.idList }
      .filter { it.key != null }

    if (itemsCheck.isNotEmpty()) {
      return processItems(itemsCheck, authToken, counted)
    }

    return counted
  }

  private suspend fun handleRemoveItems(
    removeItems: List<TraktSyncQueue>,
    authToken: TraktAuthToken,
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
        cloud.traktApi.postRemoveListItems(authToken.token, list.idTrakt!!, showIds, movieIds)
        database.traktSyncQueueDao().delete(removeItems)
      }
    } catch (error: Throwable) {
      if (error is HttpException && error.code() == 404) {
        database.traktSyncQueueDao().delete(removeItems)
        Timber.d("Tried to remove from list but it does not exist anymore. Skipping...")
      } else {
        throw error
      }
    }
  }

  private suspend fun handleAddItems(
    addItems: List<TraktSyncQueue>,
    authToken: TraktAuthToken,
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
        cloud.traktApi.postAddListItems(authToken.token, localList.idTrakt!!, showIds, movieIds)
        database.traktSyncQueueDao().delete(addItems)
      }
    } catch (error: Throwable) {
      if (error is HttpException && error.code() == 404) {
        Timber.d("Tried to add to list but it does not exist. Creating...")
        delay(TRAKT_DELAY)
        createMissingList(localList, authToken)
        database.traktSyncQueueDao().delete(addItems)
      } else {
        throw error
      }
    }
  }

  private suspend fun createMissingList(localList: CustomList, token: TraktAuthToken): CustomList {
    val result = cloud.traktApi.postCreateList(token.token, localList.name, localList.description)
      .run { mappers.customList.fromNetwork(this) }
    listsRepository.updateList(localList.id, result.idTrakt, result.idSlug, result.name, result.description)

    val localItems = listsRepository.loadListItemsForId(localList.id)
    if (localItems.isNotEmpty()) {
      val showsIds = localItems.filter { it.type == Mode.SHOWS.type }.map { it.idTrakt }
      val moviesIds = localItems.filter { it.type == Mode.MOVIES.type }.map { it.idTrakt }
      delay(TRAKT_DELAY)
      cloud.traktApi.postAddListItems(token.token, result.idTrakt!!, showsIds, moviesIds)
    }

    return listsRepository.updateList(localList.id, result.idTrakt, result.idSlug, result.name, result.description)
  }
}
