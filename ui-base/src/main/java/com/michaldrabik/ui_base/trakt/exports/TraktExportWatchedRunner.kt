package com.michaldrabik.ui_base.trakt.exports

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncExportItem
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.network.trakt.model.SyncItem
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.Episode
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import com.michaldrabik.ui_repository.TraktAuthToken
import com.michaldrabik.ui_repository.UserTraktManager
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

@AppScope
class TraktExportWatchedRunner @Inject constructor(
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
      exportWatched(authToken)
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("exportWatched failed. Will retry in $RETRY_DELAY_MS ms... $error")
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

  private suspend fun exportWatched(token: TraktAuthToken) {
    Timber.d("Exporting watched...")

    val remoteWatched = cloud.traktApi.fetchSyncWatched(token.token)
      .filter { it.show != null }
    val localMyShows = database.myShowsDao().getAll()
    val localEpisodes = batchEpisodes(localMyShows.map { it.idTrakt })
      .filter { !hasEpisodeBeenWatched(remoteWatched, it) }

    val request = SyncExportRequest(
      episodes = localEpisodes.map { ep ->
        val timestamp = localMyShows.find { it.idTrakt == ep.idShowTrakt }?.updatedAt ?: nowUtcMillis()
        SyncExportItem.create(ep.idTrakt, com.michaldrabik.common.extensions.dateIsoStringFromMillis(timestamp))
      }
    )
    cloud.traktApi.postSyncWatched(token.token, request)
  }

  private suspend fun batchEpisodes(
    showsIds: List<Long>,
    allEpisodes: MutableList<Episode> = mutableListOf()
  ): List<Episode> {
    val batch = showsIds.take(500)
    if (batch.isEmpty()) return allEpisodes

    val episodes = database.episodesDao().getAllWatchedForShows(batch)
    allEpisodes.addAll(episodes)

    return batchEpisodes(showsIds.filter { it !in batch }, allEpisodes)
  }

  private fun hasEpisodeBeenWatched(remoteWatched: List<SyncItem>, episodeDb: Episode): Boolean {
    val find = remoteWatched
      .find { it.show?.ids?.trakt == episodeDb.idShowTrakt }
      ?.seasons?.find { it.number == episodeDb.seasonNumber }
      ?.episodes?.find { it.number == episodeDb.episodeNumber }
    return find != null
  }
}
