package com.michaldrabik.showly2.common.trakt.quicksync

import android.content.Context
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.repository.settings.SettingsRepository
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.TraktSyncQueue
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

  suspend fun isAnyScheduled(): Boolean {
    if (!userTraktManager.isAuthorized()) {
      Timber.d("User not logged into Trakt. Skipping...")
      return false
    }

    return database.traktSyncQueueDao().getAll().isNotEmpty()
  }
}
