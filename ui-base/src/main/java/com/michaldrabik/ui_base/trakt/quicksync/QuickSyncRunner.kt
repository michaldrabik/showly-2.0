package com.michaldrabik.ui_base.trakt.quicksync

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.dateIsoStringFromMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncExportItem
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.TraktSyncQueue
import com.michaldrabik.storage.database.model.TraktSyncQueue.Type.EPISODE
import com.michaldrabik.storage.database.model.TraktSyncQueue.Type.SHOW_WATCHLIST
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import com.michaldrabik.ui_repository.TraktAuthToken
import com.michaldrabik.ui_repository.UserTraktManager
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

    val episodesCount = exportItems(authToken, EPISODE)
    val watchlistShowsCount = exportItems(authToken, SHOW_WATCHLIST)

    isRunning = false
    Timber.d("Finished with success.")
    return episodesCount + watchlistShowsCount
  }

  private suspend fun exportItems(
    token: TraktAuthToken,
    type: TraktSyncQueue.Type,
    count: Int = 0
  ): Int {
    val items = database.traktSyncQueueDao().getAll(type.slug).take(BATCH_LIMIT)
    if (items.isEmpty()) {
      Timber.d("Nothing to export. Cancelling..")
      return count
    }

    val toExport = items.distinctBy { it.idTrakt }
    Timber.d("Exporting ${toExport.count()} quick sync items...")
    when (type) {
      EPISODE -> {
        val request = SyncExportRequest(
          episodes = toExport.map { SyncExportItem.create(it.idTrakt, dateIsoStringFromMillis(it.updatedAt)) }
        )
        cloud.traktApi.postSyncWatched(token.token, request)
      }
      SHOW_WATCHLIST -> {
        val request = SyncExportRequest(
          shows = toExport.map { SyncExportItem.create(it.idTrakt, dateIsoStringFromMillis(it.updatedAt)) }
        )
        cloud.traktApi.postSyncWatchlist(token.token, request)
      }
    }
    database.traktSyncQueueDao().deleteAll(items.map { it.idTrakt }, type.slug)

    // Check for more items
    val newItems = database.traktSyncQueueDao().getAll(type.slug)
    if (newItems.isNotEmpty()) {
      delay(1000)
      return exportItems(token, type, count + toExport.count())
    }

    return count + toExport.count()
  }
}
