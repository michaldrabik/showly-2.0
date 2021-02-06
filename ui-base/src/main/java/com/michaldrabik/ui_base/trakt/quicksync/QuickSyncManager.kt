package com.michaldrabik.ui_base.trakt.quicksync

import android.content.Context
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.TraktSyncQueue
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.UserTraktManager
import timber.log.Timber
import javax.inject.Inject

@AppScope
class QuickSyncManager @Inject constructor(
  private val userTraktManager: UserTraktManager,
  private val settingsRepository: SettingsRepository,
  private val database: AppDatabase
) {

  suspend fun scheduleEpisodes(context: Context, episodesIds: List<Long>) {
    val settings = settingsRepository.load()
    if (!settings.traktQuickSyncEnabled) {
      Timber.d("Quick Sync is disabled. Skipping...")
      return
    }
    if (!userTraktManager.isAuthorized()) {
      Timber.d("User not logged into Trakt. Skipping...")
      return
    }

    val time = nowUtcMillis()
    val items = episodesIds.map { TraktSyncQueue.createEpisode(it, time, time) }
    database.traktSyncQueueDao().insert(items)
    Timber.d("Episodes added into sync queue. Count: ${items.size}")

    QuickSyncWorker.schedule(context)
  }

  suspend fun scheduleMovies(context: Context, moviesIds: List<Long>) {
    val settings = settingsRepository.load()
    if (!settings.traktQuickSyncEnabled) {
      Timber.d("Quick Sync is disabled. Skipping...")
      return
    }
    if (!userTraktManager.isAuthorized()) {
      Timber.d("User not logged into Trakt. Skipping...")
      return
    }

    val time = nowUtcMillis()
    val items = moviesIds.map { TraktSyncQueue.createMovie(it, time, time) }
    database.traktSyncQueueDao().insert(items)
    Timber.d("Movies added into sync queue. Count: ${items.size}")

    QuickSyncWorker.schedule(context)
  }

  suspend fun scheduleShowsWatchlist(context: Context, showsIds: List<Long>) {
    val settings = settingsRepository.load()
    if (!settings.traktQuickSyncEnabled) {
      Timber.d("Quick Sync is disabled. Skipping...")
      return
    }
    if (!userTraktManager.isAuthorized()) {
      Timber.d("User not logged into Trakt. Skipping...")
      return
    }

    val time = nowUtcMillis()
    val items = showsIds.map { TraktSyncQueue.createShowWatchlist(it, time, time) }
    database.traktSyncQueueDao().insert(items)
    Timber.d("Shows added into sync queue. Count: ${items.size}")

    QuickSyncWorker.schedule(context)
  }

  suspend fun scheduleMoviesWatchlist(context: Context, moviesIds: List<Long>) {
    val settings = settingsRepository.load()
    if (!settings.traktQuickSyncEnabled) {
      Timber.d("Quick Sync is disabled. Skipping...")
      return
    }
    if (!userTraktManager.isAuthorized()) {
      Timber.d("User not logged into Trakt. Skipping...")
      return
    }

    val time = nowUtcMillis()
    val items = moviesIds.map { TraktSyncQueue.createMovieWatchlist(it, time, time) }
    database.traktSyncQueueDao().insert(items)
    Timber.d("Movies added into sync queue. Count: ${items.size}")

    QuickSyncWorker.schedule(context)
  }

  suspend fun clearEpisodes(episodesIds: List<Long>) {
    val settings = settingsRepository.load()
    if (!settings.traktQuickSyncEnabled) {
      Timber.d("Quick Sync is disabled. Skipping...")
      return
    }

    val count = database.traktSyncQueueDao().deleteAll(episodesIds, TraktSyncQueue.Type.EPISODE.slug)
    Timber.d("Episodes removed from sync queue. Count: $count")
  }

  suspend fun clearMovies(moviesIds: List<Long>) {
    val settings = settingsRepository.load()
    if (!settings.traktQuickSyncEnabled) {
      Timber.d("Quick Sync is disabled. Skipping...")
      return
    }

    database.traktSyncQueueDao().deleteAll(moviesIds, TraktSyncQueue.Type.MOVIE.slug)
    Timber.d("Movies removed from sync queue. Count: ${moviesIds.size}")
  }

  suspend fun clearShowsWatchlist(showsIds: List<Long>) {
    val settings = settingsRepository.load()
    if (!settings.traktQuickSyncEnabled) {
      Timber.d("Quick Sync is disabled. Skipping...")
      return
    }

    database.traktSyncQueueDao().deleteAll(showsIds, TraktSyncQueue.Type.SHOW_WATCHLIST.slug)
    Timber.d("Shows removed from sync queue. Count: ${showsIds.size}")
  }

  suspend fun clearMoviesWatchlist(moviesIds: List<Long>) {
    val settings = settingsRepository.load()
    if (!settings.traktQuickSyncEnabled) {
      Timber.d("Quick Sync is disabled. Skipping...")
      return
    }

    database.traktSyncQueueDao().deleteAll(moviesIds, TraktSyncQueue.Type.MOVIE_WATCHLIST.slug)
    Timber.d("Movies removed from sync queue. Count: ${moviesIds.size}")
  }

  suspend fun isAnyScheduled(): Boolean {
    if (!userTraktManager.isAuthorized()) {
      Timber.d("User not logged into Trakt. Skipping...")
      return false
    }

    return database.traktSyncQueueDao().getAll().isNotEmpty()
  }
}
