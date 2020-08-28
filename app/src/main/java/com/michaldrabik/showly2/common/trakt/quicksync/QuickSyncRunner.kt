package com.michaldrabik.showly2.common.trakt.quicksync

import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncExportItem
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.showly2.common.trakt.TraktSyncRunner
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.repository.TraktAuthToken
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.utilities.extensions.dateIsoStringFromMillis
import com.michaldrabik.storage.database.AppDatabase
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

@AppScope
class QuickSyncRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  companion object {
    private const val BATCH_LIMIT = 50
  }

  override suspend fun run(): Int {
    Timber.d("Initialized.")
    isRunning = true

    Timber.d("Checking authorization...")
    val authToken = checkAuthorization()
    val count = exportItems(authToken)

    isRunning = false
    Timber.d("Finished with success.")
    return count
  }

  private suspend fun exportItems(token: TraktAuthToken, count: Int = 0): Int {
    val items = database.traktSyncQueueDao().getAll().take(BATCH_LIMIT)
    if (items.isEmpty()) {
      Timber.d("Nothing to export. Cancelling..")
      return count
    }

    val toExport = items.distinctBy { it.idTrakt }
    val request = SyncExportRequest(
      episodes = toExport.map { SyncExportItem.create(it.idTrakt, dateIsoStringFromMillis(it.updatedAt)) }
    )
    Timber.d("Exporting ${toExport.count()} quick sync items...")
    cloud.traktApi.postSyncWatched(token.token, request)
    database.traktSyncQueueDao().deleteAll(items.map { it.idTrakt })

    // Check for more items
    val newItems = database.traktSyncQueueDao().getAll()
    if (newItems.isNotEmpty()) {
      delay(1000)
      return exportItems(token, count + toExport.count())
    }

    return count + toExport.count()
  }
}
