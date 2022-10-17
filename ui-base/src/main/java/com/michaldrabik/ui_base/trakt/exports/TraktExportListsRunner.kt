package com.michaldrabik.ui_base.trakt.exports

import com.michaldrabik.common.Mode
import com.michaldrabik.common.errors.ErrorHelper
import com.michaldrabik.common.errors.ShowlyError
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_remote.trakt.TraktRemoteDataSource
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import com.michaldrabik.ui_model.CustomList
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktExportListsRunner @Inject constructor(
  private val remoteSource: TraktRemoteDataSource,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val settingsRepository: SettingsRepository,
  userTraktManager: UserTraktManager,
) : TraktSyncRunner(userTraktManager) {

  private var hasAccountLimitsOccurred = false

  override suspend fun run(): Int {
    Timber.d("Initialized.")
    hasAccountLimitsOccurred = false

    checkAuthorization()
    runExport()

    if (hasAccountLimitsOccurred) {
      throw ShowlyError.AccountLimitsError
    }
    Timber.d("Finished with success.")
    return 0
  }

  private suspend fun runExport() {
    try {
      exportLists()
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("exportLists failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        runExport()
      } else {
        throw error
      }
    }
  }

  private suspend fun exportLists() {
    Timber.d("Exporting lists...")

    val localLists = localSource.customLists.getAll()
      .map { mappers.customList.fromDatabase(it) }
    val remoteLists = remoteSource.fetchSyncLists()
      .map { mappers.customList.fromNetwork(it) }

    localLists
      .sortedByDescending { it.updatedAt }
      .forEach { localList ->
        Timber.d("Processing ${localList.name}...")
        try {
          val isNewList = remoteLists.none { it.idTrakt == localList.idTrakt }
          if (isNewList) {
            Timber.d("List not found in Trakt. Creating and uploading items...")
            exportNewList(localList)
          } else {
            Timber.d("List found in Trakt.")
            exportExistingList(remoteLists, localList)
          }
        } catch (error: Throwable) {
          val showlyError = ErrorHelper.parse(error)
          if (showlyError == ShowlyError.AccountLimitsError) {
            Timber.w("Account limits reached. Skipping the rest of lists exporting.")
            hasAccountLimitsOccurred = true
            return@forEach
          }
          throw error
        }
      }
  }

  private suspend fun exportNewList(localList: CustomList) {
    val listNet = remoteSource.postCreateList(localList.name, localList.description)
    Timber.d("List created in Trakt.")

    val list = mappers.customList.fromNetwork(listNet)
    val listDb = mappers.customList.toDatabase(list).copy(id = localList.id)

    localSource.customLists.update(listOf(listDb))
    delay(TRAKT_LIMIT_DELAY_MS)

    val localItems = localSource.customListsItems.getItemsById(localList.id)
    if (localItems.isNotEmpty()) {
      val showsIds = localItems.filter { it.type == Mode.SHOWS.type }.map { it.idTrakt }
      val moviesIds = localItems.filter { it.type == Mode.MOVIES.type }.map { it.idTrakt }
      remoteSource.postAddListItems(listNet.ids.trakt, showsIds, moviesIds)
      Timber.d("Items added into Trakt list.")
      delay(TRAKT_LIMIT_DELAY_MS)
    }
  }

  private suspend fun exportExistingList(
    remoteLists: List<CustomList>,
    localList: CustomList,
  ) {
    val moviesEnabled = settingsRepository.isMoviesEnabled

    val remoteList = remoteLists.first { it.idTrakt == localList.idTrakt }
    if (localList.updatedAt.isEqual(remoteList.updatedAt)) {
      Timber.d("Timestamps are the same.")
      return
    }

    Timber.d("Timestamps are different.")
    if (localList.updatedAt.isAfter(remoteList.updatedAt)) {
      Timber.d("Local list timestamp is newer.")
      if (localList.name != remoteList.name || localList.description != remoteList.description) {
        Timber.d("Name or description are different. Updating...")
        val updateList = mappers.customList.toNetwork(localList)
        val resultList = remoteSource.postUpdateList(updateList)
        val listDb = mappers.customList.fromNetwork(resultList).copy(id = localList.id)
        localSource.customLists.update(listOf(mappers.customList.toDatabase(listDb)))
        delay(TRAKT_LIMIT_DELAY_MS)
      }
    }

    Timber.d("Processing list items...")
    val listTraktId = localList.idTrakt!!
    val remoteItems = remoteSource.fetchSyncListItems(listTraktId, moviesEnabled)
      .filter { it.movie != null || it.show != null }
    val localItems = localSource.customListsItems.getItemsById(localList.id)
      .filter { localItem ->
        remoteItems.none {
          it.getTraktId() == localItem.idTrakt && it.getType() == localItem.type
        }
      }

    if (localItems.isNotEmpty()) {
      Timber.d("${localItems.size} to be exported...")

      val showsIds = localItems.filter { it.type == Mode.SHOWS.type }.map { it.idTrakt }
      val moviesIds = localItems.filter { it.type == Mode.MOVIES.type }.map { it.idTrakt }
      remoteSource.postAddListItems(listTraktId, showsIds, moviesIds)

      Timber.d("Exported!")
      delay(TRAKT_LIMIT_DELAY_MS)
    }

    updateListTimestamp(localList.id, listTraktId)
  }

  private suspend fun updateListTimestamp(listId: Long, listTraktId: Long) {
    try {
      Timber.d("Updating timestamp...")
      val list = remoteSource.fetchSyncList(listTraktId)
        .run { mappers.customList.fromNetwork(this) }
      localSource.customLists.updateTimestamp(listId, list.updatedAt.toMillis())
      Timber.d("Local list timestamp updated.")
    } catch (error: Throwable) {
      Timber.w(error)
      // Skip timestamp update in case of failure.
    }
  }
}
