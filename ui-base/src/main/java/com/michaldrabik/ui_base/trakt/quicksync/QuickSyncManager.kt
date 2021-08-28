package com.michaldrabik.ui_base.trakt.quicksync

import androidx.work.WorkManager
import com.michaldrabik.common.Mode
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.TraktSyncQueue
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Operation
import com.michaldrabik.data_local.database.model.TraktSyncQueue.Type
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_base.utilities.extensions.runTransaction
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuickSyncManager @Inject constructor(
  private val userTraktManager: UserTraktManager,
  private val settingsRepository: SettingsRepository,
  private val database: AppDatabase,
  private val workManager: WorkManager
) {

  suspend fun scheduleEpisodes(episodesIds: List<Long>) {
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

    QuickSyncWorker.schedule(workManager)
  }

  suspend fun scheduleMovies(moviesIds: List<Long>) {
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

    QuickSyncWorker.schedule(workManager)
  }

  suspend fun scheduleShowsWatchlist(showsIds: List<Long>) {
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

    QuickSyncWorker.schedule(workManager)
  }

  suspend fun scheduleMoviesWatchlist(moviesIds: List<Long>) {
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

    QuickSyncWorker.schedule(workManager)
  }

  suspend fun scheduleAddToList(idTrakt: Long, idList: Long, type: Mode) {
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
    val item = when (type) {
      Mode.SHOWS -> TraktSyncQueue.createListShow(idTrakt, idList, Operation.ADD, time, time)
      Mode.MOVIES -> TraktSyncQueue.createListMovie(idTrakt, idList, Operation.ADD, time, time)
    }

    val itemType = when (type) {
      Mode.SHOWS -> Type.LIST_ITEM_SHOW
      Mode.MOVIES -> Type.LIST_ITEM_MOVIE
    }

    database.runTransaction {
      traktSyncQueueDao().delete(idTrakt, idList, itemType.slug, Operation.ADD.slug)
      val count = traktSyncQueueDao().delete(idTrakt, idList, itemType.slug, Operation.REMOVE.slug)
      if (count == 0) {
        traktSyncQueueDao().insert(listOf(item))
        Timber.d("Added ${type.type} list item into add to list queue.")
      }
    }

    QuickSyncWorker.schedule(workManager)
  }

  suspend fun scheduleRemoveFromList(idTrakt: Long, idList: Long, type: Mode) {
    val settings = settingsRepository.load()
    if (!settings.traktQuickSyncEnabled) {
      Timber.d("Quick Sync/Remove is disabled. Skipping...")
      return
    }
    if (!userTraktManager.isAuthorized()) {
      Timber.d("User not logged into Trakt. Skipping...")
      return
    }

    val time = nowUtcMillis()
    val item = when (type) {
      Mode.SHOWS -> TraktSyncQueue.createListShow(idTrakt, idList, Operation.REMOVE, time, time)
      Mode.MOVIES -> TraktSyncQueue.createListMovie(idTrakt, idList, Operation.REMOVE, time, time)
    }

    val itemType = when (type) {
      Mode.SHOWS -> Type.LIST_ITEM_SHOW
      Mode.MOVIES -> Type.LIST_ITEM_MOVIE
    }

    database.runTransaction {
      traktSyncQueueDao().delete(idTrakt, idList, itemType.slug, Operation.REMOVE.slug)
      val count = traktSyncQueueDao().delete(idTrakt, idList, itemType.slug, Operation.ADD.slug)
      if (count == 0 && settings.traktQuickRemoveEnabled) {
        traktSyncQueueDao().insert(listOf(item))
        Timber.d("Added ${type.type} list item into remove from list queue.")
      }
    }

    QuickSyncWorker.schedule(workManager)
  }

  suspend fun clearEpisodes(episodesIds: List<Long>) {
    val settings = settingsRepository.load()
    if (!settings.traktQuickSyncEnabled) {
      Timber.d("Quick Sync is disabled. Skipping...")
      return
    }

    val count = database.traktSyncQueueDao().deleteAll(episodesIds, Type.EPISODE.slug)
    Timber.d("Episodes removed from sync queue. Count: $count")
  }

  suspend fun clearMovies(moviesIds: List<Long>) {
    val settings = settingsRepository.load()
    if (!settings.traktQuickSyncEnabled) {
      Timber.d("Quick Sync is disabled. Skipping...")
      return
    }

    database.traktSyncQueueDao().deleteAll(moviesIds, Type.MOVIE.slug)
    Timber.d("Movies removed from sync queue. Count: ${moviesIds.size}")
  }

  suspend fun clearShowsWatchlist(showsIds: List<Long>) {
    val settings = settingsRepository.load()
    if (!settings.traktQuickSyncEnabled) {
      Timber.d("Quick Sync is disabled. Skipping...")
      return
    }

    database.traktSyncQueueDao().deleteAll(showsIds, Type.SHOW_WATCHLIST.slug)
    Timber.d("Shows removed from sync queue. Count: ${showsIds.size}")
  }

  suspend fun clearMoviesWatchlist(moviesIds: List<Long>) {
    val settings = settingsRepository.load()
    if (!settings.traktQuickSyncEnabled) {
      Timber.d("Quick Sync is disabled. Skipping...")
      return
    }

    database.traktSyncQueueDao().deleteAll(moviesIds, Type.MOVIE_WATCHLIST.slug)
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
