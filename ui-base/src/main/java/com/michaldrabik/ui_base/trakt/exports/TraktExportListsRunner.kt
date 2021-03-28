package com.michaldrabik.ui_base.trakt.exports

import com.michaldrabik.common.Mode
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TraktAuthToken
import com.michaldrabik.ui_repository.UserTraktManager
import com.michaldrabik.ui_repository.mappers.Mappers
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

@AppScope
class TraktExportListsRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val settingsRepository: SettingsRepository,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  override suspend fun run(): Int {
    Timber.d("Initialized.")
    isRunning = true

    val authToken = checkAuthorization()
    runExport(authToken)

    isRunning = false
    Timber.d("Finished with success.")
    return 0
  }

  private suspend fun runExport(authToken: TraktAuthToken) {
    try {
      exportLists(authToken)
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("exportLists failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        runExport(authToken)
      } else {
        isRunning = false
        throw error
      }
    }
  }

  private suspend fun exportLists(token: TraktAuthToken) {
    Timber.d("Exporting lists...")

    val localLists = database.customListsDao().getAll()
      .map { mappers.customList.fromDatabase(it) }
    val remoteLists = cloud.traktApi.fetchSyncLists(token.token)
      .map { mappers.customList.fromNetwork(it) }

    localLists.forEach { localList ->
      if (remoteLists.none { it.idTrakt == localList.idTrakt }) {
        val listNet = cloud.traktApi.postCreateList(token.token, localList.name, localList.description)
        val list = mappers.customList.fromNetwork(listNet)
        val listDb = mappers.customList.toDatabase(list).copy(id = localList.id)
        database.customListsDao().update(listOf(listDb))

        val localItems = database.customListsItemsDao().getItemsById(localList.id)
        val showsIds = localItems.filter { it.type == Mode.SHOWS.type }.map { it.idTrakt }
        val moviesIds = localItems.filter { it.type == Mode.MOVIES.type }.map { it.idTrakt }

        delay(1000)
        cloud.traktApi.postAddListItems(token.token, listNet.ids.trakt, showsIds, moviesIds)
      } else {
        val remoteList = remoteLists.first { it.idTrakt == localList.idTrakt }
        if (localList.updatedAt.isAfter(remoteList.updatedAt)) {
          if (localList.name != remoteList.name || localList.description != remoteList.description) {
            val updateList = mappers.customList.toNetwork(localList)
            val resultList = cloud.traktApi.postUpdateList(token.token, updateList)
            val listDb = mappers.customList.fromNetwork(resultList).copy(id = localList.id)
            database.customListsDao().update(listOf(mappers.customList.toDatabase(listDb)))
            delay(1000)
          }
        }
        // TODO if updatedAt are different fetch all the items and send missing ones.
      }
    }
  }
}
