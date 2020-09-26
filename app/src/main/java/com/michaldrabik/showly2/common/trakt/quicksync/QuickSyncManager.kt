package com.michaldrabik.showly2.common.trakt.quicksync

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

  suspend fun scheduleShowsSeeLater(context: Context, showsIds: List<Long>) {
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
    val items = showsIds.map { TraktSyncQueue.createShowSeeLater(it, time, time) }
    database.traktSyncQueueDao().insert(items)
    Timber.d("Shows added into sync queue. Count: ${items.size}")

    QuickSyncWorker.schedule(context)
  }

  suspend fun clearShowsSeeLater(showsIds: List<Long>) {
    val settings = settingsRepository.load()
    if (!settings.traktQuickSyncEnabled) {
      Timber.d("Quick Sync is disabled. Skipping...")
      return
    }

    database.traktSyncQueueDao().deleteAll(showsIds, TraktSyncQueue.Type.SHOW_SEE_LATER.slug)
    Timber.d("Shows removed from sync queue. Count: ${showsIds.size}")
  }

  suspend fun isAnyScheduled(): Boolean {
    if (!userTraktManager.isAuthorized()) {
      Timber.d("User not logged into Trakt. Skipping...")
      return false
    }

    return database.traktSyncQueueDao().getAll().isNotEmpty()
  }
}
