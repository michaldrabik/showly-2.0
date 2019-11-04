package com.michaldrabik.showly2.common

import android.util.Log
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.ShowStatus.CANCELED
import com.michaldrabik.showly2.model.ShowStatus.ENDED
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.ui.show.seasons.episodes.EpisodesInteractor
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * This class is responsible for fetching and syncing missing/updated episodes data for current watchlist items.
 */
@AppScope
class EpisodesSynchronizer @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val episodesInteractor: EpisodesInteractor
) {

  companion object {
    private const val TAG = "EpisodesSynchronizer"
  }

  suspend fun synchronize() {
    Log.i(TAG, "Sync initialized.")

    val showsToSync = database.followedShowsDao().getAll()
      .filter { it.status !in arrayOf(ENDED.key, CANCELED.key) }

    Log.i(TAG, "Shows to sync: ${showsToSync.size}.")

    if (showsToSync.isEmpty()) {
      Log.i(TAG, "Nothing to sync.")
      return
    }

    val syncLog = database.episodesSyncLogDao().getAll()
    showsToSync.forEach { showDb ->
      Log.i(TAG, "Syncing ${showDb.title}(${showDb.idTrakt})...")

      val lastSync = syncLog.find { it.idTrakt == showDb.idTrakt }?.syncedAt ?: 0
      if (nowUtcMillis() - lastSync < Config.SHOW_SYNC_COOLDOWN) {
        Log.i(TAG, "${showDb.title} is on cooldown. No need to sync.")
        return@forEach
      }

      try {
        val show = mappers.show.fromDatabase(showDb)
        val remoteEpisodes = cloud.traktApi.fetchSeasons(show.ids.trakt.id)
          .map { mappers.season.fromNetwork(it) }

        episodesInteractor.invalidateEpisodes(show, remoteEpisodes)

        Log.i(TAG, "${show.title}(${showDb.idTrakt}) synced.")
      } catch (t: Throwable) {
        Log.w(TAG, "${showDb.title}(${showDb.idTrakt}) sync error. Skipping... \n$t")
      } finally {
        delay(200)
      }
    }
  }
}