package com.michaldrabik.ui_base.trakt.quicksync

import androidx.room.withTransaction
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.dateIsoStringFromMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncExportItem
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.TraktSyncQueue.Type.EPISODE
import com.michaldrabik.storage.database.model.TraktSyncQueue.Type.MOVIE
import com.michaldrabik.storage.database.model.TraktSyncQueue.Type.MOVIE_WATCHLIST
import com.michaldrabik.storage.database.model.TraktSyncQueue.Type.SHOW_WATCHLIST
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TraktAuthToken
import com.michaldrabik.ui_repository.UserTraktManager
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

@AppScope
class QuickSyncRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val settingsRepository: SettingsRepository,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  companion object {
    private const val BATCH_LIMIT = 100
    private const val DELAY = 2000L
  }

  override suspend fun run(): Int {
    Timber.d("Initialized.")
    isRunning = true

    Timber.d("Checking authorization...")
    val authToken = checkAuthorization()
    val moviesEnabled = settingsRepository.load().moviesEnabled

    val historyCount = exportHistoryItems(authToken, moviesEnabled)
    val watchlistCount = exportWatchlistItems(authToken, moviesEnabled)

    isRunning = false
    Timber.d("Finished with success.")
    return historyCount + watchlistCount
  }

  private suspend fun exportHistoryItems(
    token: TraktAuthToken,
    moviesEnabled: Boolean,
    count: Int = 0,
  ): Int {
    val types = if (moviesEnabled) listOf(MOVIE, EPISODE) else listOf(EPISODE)
    val items = database.traktSyncQueueDao().getAll(types.map { it.slug }).take(BATCH_LIMIT)
    if (items.isEmpty()) {
      Timber.d("Nothing to export. Cancelling..")
      return count
    }

    val exportEpisodes = items.filter { it.type == EPISODE.slug }.distinctBy { it.idTrakt }
    val exportMovies = items.filter { it.type == MOVIE.slug }.distinctBy { it.idTrakt }

    val request = SyncExportRequest(
      episodes = exportEpisodes.map { SyncExportItem.create(it.idTrakt, dateIsoStringFromMillis(it.updatedAt)) },
      movies = exportMovies.map { SyncExportItem.create(it.idTrakt, dateIsoStringFromMillis(it.updatedAt)) }
    )

    cloud.traktApi.postSyncWatched(token.token, request)
    database.withTransaction {
      val ids = items.map { it.idTrakt }
      database.traktSyncQueueDao().deleteAll(ids, EPISODE.slug)
      database.traktSyncQueueDao().deleteAll(ids, MOVIE.slug)
    }

    val currentCount = count + exportEpisodes.count() + exportMovies.count()

    // Check for more items
    val newItems = database.traktSyncQueueDao().getAll(types.map { it.slug })
    if (newItems.isNotEmpty()) {
      delay(DELAY)
      return exportHistoryItems(token, moviesEnabled, currentCount)
    }

    return currentCount
  }

  private suspend fun exportWatchlistItems(
    token: TraktAuthToken,
    moviesEnabled: Boolean,
    count: Int = 0,
  ): Int {
    val types = if (moviesEnabled) listOf(MOVIE_WATCHLIST, SHOW_WATCHLIST) else listOf(SHOW_WATCHLIST)
    val items = database.traktSyncQueueDao().getAll(types.map { it.slug }).take(BATCH_LIMIT)
    if (items.isEmpty()) {
      Timber.d("Nothing to export. Cancelling..")
      return count
    }

    val exportShows = items.filter { it.type == SHOW_WATCHLIST.slug }.distinctBy { it.idTrakt }
    val exportMovies = items.filter { it.type == MOVIE_WATCHLIST.slug }.distinctBy { it.idTrakt }

    val request = SyncExportRequest(
      shows = exportShows.map { SyncExportItem.create(it.idTrakt, dateIsoStringFromMillis(it.updatedAt)) },
      movies = exportMovies.map { SyncExportItem.create(it.idTrakt, dateIsoStringFromMillis(it.updatedAt)) }
    )

    cloud.traktApi.postSyncWatchlist(token.token, request)
    database.withTransaction {
      val ids = items.map { it.idTrakt }
      database.traktSyncQueueDao().deleteAll(ids, MOVIE_WATCHLIST.slug)
      database.traktSyncQueueDao().deleteAll(ids, SHOW_WATCHLIST.slug)
    }

    val currentCount = count + exportShows.count() + exportMovies.count()

    // Check for more items
    val newItems = database.traktSyncQueueDao().getAll(types.map { it.slug })
    if (newItems.isNotEmpty()) {
      delay(DELAY)
      return exportWatchlistItems(token, moviesEnabled, currentCount)
    }

    return currentCount
  }
}
