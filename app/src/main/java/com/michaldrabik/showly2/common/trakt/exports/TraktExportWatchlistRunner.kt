package com.michaldrabik.showly2.common.trakt.exports

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncExportItem
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.showly2.common.trakt.TraktSyncRunner
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_repository.TraktAuthToken
import com.michaldrabik.ui_repository.UserTraktManager
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
    exportWatchlist(authToken)

    isRunning = false
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
