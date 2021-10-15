package com.michaldrabik.ui_base.trakt.quicksync.runners

import androidx.room.withTransaction
import com.michaldrabik.common.extensions.dateIsoStringFromMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Type.EPISODE
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Type.HIDDEN_MOVIE
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Type.HIDDEN_SHOW
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Type.MOVIE
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Type.MOVIE_WATCHLIST
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Type.SHOW_WATCHLIST
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.data_remote.trakt.model.SyncExportItem
import com.michaldrabik.data_remote.trakt.model.SyncExportRequest
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.TraktAuthToken
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import com.michaldrabik.ui_base.utilities.extensions.runTransaction
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
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

    val authToken = checkAuthorization()
    val moviesEnabled = settingsRepository.isMoviesEnabled

    val historyCount = exportHistoryItems(authToken, moviesEnabled)
    val watchlistCount = exportWatchlistItems(authToken, moviesEnabled)
    val hiddenCount = exportHiddenItems(authToken, moviesEnabled)

    isRunning = false
    Timber.d("Finished with success.")
    return historyCount + watchlistCount + hiddenCount
  }

  private suspend fun exportHistoryItems(
    token: TraktAuthToken,
    moviesEnabled: Boolean,
    count: Int = 0
  ): Int {
    val types = if (moviesEnabled) listOf(MOVIE, EPISODE) else listOf(EPISODE)
    val items = database.traktSyncQueueDao().getAll(types.map { it.slug }).take(BATCH_LIMIT)
    if (items.isEmpty()) {
      Timber.d("Nothing to export. Cancelling..")
      return count
    }

    Timber.d("Exporting history items...")

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
    count: Int = 0
  ): Int {
    val types = if (moviesEnabled) listOf(MOVIE_WATCHLIST, SHOW_WATCHLIST) else listOf(SHOW_WATCHLIST)
    val items = database.traktSyncQueueDao().getAll(types.map { it.slug }).take(BATCH_LIMIT)
    if (items.isEmpty()) {
      Timber.d("Nothing to export. Cancelling..")
      return count
    }

    Timber.d("Exporting watchlist items...")

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

  private suspend fun exportHiddenItems(
    token: TraktAuthToken,
    moviesEnabled: Boolean,
    count: Int = 0
  ): Int {
    val types = if (moviesEnabled) listOf(HIDDEN_SHOW, HIDDEN_MOVIE) else listOf(HIDDEN_SHOW)
    val items = database.traktSyncQueueDao().getAll(types.map { it.slug }).take(BATCH_LIMIT)
    if (items.isEmpty()) {
      Timber.d("Nothing to export. Cancelling..")
      return count
    }

    Timber.d("Exporting hidden items...")

    val exportShows = items.filter { it.type == HIDDEN_SHOW.slug }.distinctBy { it.idTrakt }
    val exportMovies = items.filter { it.type == HIDDEN_MOVIE.slug }.distinctBy { it.idTrakt }

    if (exportShows.isNotEmpty()) {
      cloud.traktApi.postHiddenShows(
        token.token,
        shows = exportShows.map { SyncExportItem.create(it.idTrakt, hiddenAt = dateIsoStringFromMillis(it.updatedAt)) }
      )
      delay(1500)
    }

    if (exportMovies.isNotEmpty()) {
      cloud.traktApi.postHiddenMovies(
        token.token,
        movies = exportMovies.map { SyncExportItem.create(it.idTrakt, hiddenAt = dateIsoStringFromMillis(it.updatedAt)) }
      )
    }

    database.runTransaction {
      val ids = items.map { it.idTrakt }
      with(traktSyncQueueDao()) {
        deleteAll(ids, HIDDEN_SHOW.slug)
        deleteAll(ids, HIDDEN_MOVIE.slug)
      }
    }

    val currentCount = count + exportShows.count() + exportMovies.count()

    // Check for more items
    val newItems = database.traktSyncQueueDao().getAll(types.map { it.slug })
    if (newItems.isNotEmpty()) {
      delay(DELAY)
      return exportHiddenItems(token, moviesEnabled, currentCount)
    }

    return currentCount
  }
}
