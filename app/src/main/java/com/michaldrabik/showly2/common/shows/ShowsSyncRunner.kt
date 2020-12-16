package com.michaldrabik.showly2.common.shows

import com.michaldrabik.common.Config.SHOW_SYNC_COOLDOWN
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.EpisodesSyncLog
import com.michaldrabik.ui_episodes.EpisodesManager
import com.michaldrabik.ui_model.ShowStatus.CANCELED
import com.michaldrabik.ui_model.ShowStatus.ENDED
import com.michaldrabik.ui_model.ShowStatus.UNKNOWN
import com.michaldrabik.ui_repository.mappers.Mappers
import com.michaldrabik.ui_repository.shows.ShowsRepository
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

/**
 * This class is responsible for fetching and syncing missing/updated episodes data for current progress shows.
 */
@AppScope
class ShowsSyncRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val episodesManager: EpisodesManager,
  private val showsRepository: ShowsRepository
) {

  companion object {
    private const val DELAY_MS = 10L
  }

  suspend fun run(): Int {
    Timber.i("Shows sync initialized.")

    val myShows = showsRepository.myShows.loadAll()
    val watchlistShows = showsRepository.watchlistShows.loadAll()
    val watchlistShowsIds = watchlistShows.map { it.traktId }

    val showsToSync = (myShows + watchlistShows)
      .filter { it.status !in arrayOf(ENDED, CANCELED, UNKNOWN) }

    if (showsToSync.isEmpty()) {
      Timber.i("Nothing to process. Exiting...")
      return 0
    }
    Timber.i("Shows to sync: ${showsToSync.size}.")

    var syncCount = 0
    val syncLog = database.episodesSyncLogDao().getAll()
    showsToSync.forEach { show ->
      val isInWatchlist = show.traktId in watchlistShowsIds

      val lastSync = syncLog.find { it.idTrakt == show.ids.trakt.id }?.syncedAt ?: 0
      if (nowUtcMillis() - lastSync < SHOW_SYNC_COOLDOWN) {
        Timber.i("${show.title} is on cooldown. No need to sync.")
        return@forEach
      }

      try {
        Timber.i("Syncing ${show.title}(${show.ids.trakt}) details...")
        showsRepository.detailsShow.load(show.ids.trakt, force = true)
        syncCount++
        Timber.i("${show.title}(${show.ids.trakt}) show synced.")
      } catch (t: Throwable) {
        Timber.e("${show.title}(${show.ids.trakt}) show sync error. Skipping... \n$t")
      }

      if (isInWatchlist) {
        database.episodesSyncLogDao().upsert(EpisodesSyncLog(show.ids.trakt.id, nowUtcMillis()))
      } else {
        try {
          Timber.i("Syncing ${show.title}(${show.ids.trakt}) episodes...")

          val remoteEpisodes = cloud.traktApi.fetchSeasons(show.ids.trakt.id)
            .map { mappers.season.fromNetwork(it) }
          episodesManager.invalidateEpisodes(show, remoteEpisodes)
          syncCount++

          Timber.i("${show.title}(${show.ids.trakt}) episodes synced.")
        } catch (t: Throwable) {
          Timber.e("${show.title}(${show.ids.trakt}) episodes sync error. Skipping... \n$t")
        } finally {
          delay(DELAY_MS)
        }
      }
    }

    return syncCount
  }
}
