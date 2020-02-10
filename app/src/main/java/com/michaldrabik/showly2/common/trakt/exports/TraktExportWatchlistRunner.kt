package com.michaldrabik.showly2.common.trakt.exports

import android.util.Log
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.Ids
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.network.trakt.model.SyncExportShow
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.error.TraktAuthError
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.TraktAuthToken
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class TraktExportWatchlistRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val userTraktManager: UserTraktManager
) {

  var isRunning = false

  suspend fun run() {
    isRunning = true
    Log.d(TAG, "Initialized.")
    val authToken: TraktAuthToken
    try {
      Log.d(TAG, "Checking authorization...")
      authToken = userTraktManager.checkAuthorization()
    } catch (t: Throwable) {
      isRunning = false
      throw TraktAuthError(t.message)
    }

    exportWatchlist(authToken)

    isRunning = false
    Log.d(TAG, "Finished with success.")
  }

  private suspend fun exportWatchlist(token: TraktAuthToken) {
    Log.d(TAG, "Exporting watchlist...")

    val localShows = database.seeLaterShowsDao().getAll()
      .map { mappers.show.fromDatabase(it) }
      .map { SyncExportShow(Ids(
        it.ids.trakt.id,
        it.ids.tvdb.id,
        it.ids.tmdb.id,
        it.ids.tvrage.id,
        it.ids.imdb.id,
        it.ids.slug.id
      )) }

    val request = SyncExportRequest(localShows)
    cloud.traktApi.postSyncWatchlist(token.token, request)
  }

  companion object {
    private const val TAG = "TraktExportWatchlist"
  }
}
