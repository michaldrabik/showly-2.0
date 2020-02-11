package com.michaldrabik.showly2.common.trakt.exports

import android.util.Log
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncExportItem
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.showly2.common.trakt.TraktSyncRunner
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.repository.TraktAuthToken
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class TraktExportWatchedRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  override suspend fun run(): Int {
    Log.d(TAG, "Initialized.")
    isRunning = true

    Log.d(TAG, "Checking authorization...")
    val authToken = checkAuthorization()
    exportWatched(authToken)

    isRunning = false
    Log.d(TAG, "Finished with success.")
    return 0
  }

  //TODO Do not duplicate history items!
  private suspend fun exportWatched(token: TraktAuthToken) {
    Log.d(TAG, "Exporting watched...")

    val localMyShowsIds = database.myShowsDao().getAllTraktIds()
    val localSeasons = database.seasonsDao().getAllWatchedIdsForShows(localMyShowsIds)
    val localEpisodes = database.episodesDao().getAllWatchedForShows(localMyShowsIds)
      .filter { !localSeasons.contains(it.idSeason) }

    val request = SyncExportRequest(
      episodes = localEpisodes.map { SyncExportItem.create(it.idTrakt) },
      seasons = localSeasons.map { SyncExportItem.create(it) }
    )
    cloud.traktApi.postSyncWatched(token.token, request)
  }

  companion object {
    private const val TAG = "TraktExportWatched"
  }
}
