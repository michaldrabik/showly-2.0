package com.michaldrabik.ui_base.trakt.exports

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncExportItem
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import com.michaldrabik.ui_repository.TraktAuthToken
import com.michaldrabik.ui_repository.UserTraktManager
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

@AppScope
class TraktExportWatchlistRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  override suspend fun run(): Int {
    Timber.d("Initialized.")
    isRunning = true

    Timber.d("Checking authorization...")
    val authToken = checkAuthorization()

    try {
      exportWatchlist(authToken)
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("exportWatchlist failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        run()
      } else {
        isRunning = false
        retryCount = 0
        throw error
      }
    }

    isRunning = false
    retryCount = 0

    Timber.d("Finished with success.")
    return 0
  }

  private suspend fun exportWatchlist(token: TraktAuthToken) {
    Timber.d("Exporting watchlist...")

    val localShows = database.seeLaterShowsDao().getAll()
      .map { SyncExportItem.create(it.idTrakt) }

    val request = SyncExportRequest(shows = localShows)
    cloud.traktApi.postSyncWatchlist(token.token, request)
  }
}
