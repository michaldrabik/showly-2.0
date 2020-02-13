package com.michaldrabik.showly2.common.trakt.exports

import android.util.Log
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncExportItem
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.showly2.common.trakt.TraktSyncRunner
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.repository.TraktAuthToken
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class TraktExportWatchlistRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  override suspend fun run(): Int {
    Log.d(TAG, "Initialized.")
    isRunning = true

    Log.d(TAG, "Checking authorization...")
    val authToken = checkAuthorization()
    exportWatchlist(authToken)

    isRunning = false
    Log.d(TAG, "Finished with success.")
    return 0
  }

  private suspend fun exportWatchlist(token: TraktAuthToken) {
    Log.d(TAG, "Exporting watchlist...")

    val localShows = database.seeLaterShowsDao().getAll()
      .map { SyncExportItem.create(it.idTrakt) }

    val request = SyncExportRequest(shows = localShows)
    cloud.traktApi.postSyncWatchlist(token.token, request)
  }

  companion object {
    private const val TAG = "TraktExportWatchlist"
  }
}
