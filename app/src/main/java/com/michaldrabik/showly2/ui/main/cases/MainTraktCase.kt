package com.michaldrabik.showly2.ui.main.cases

import android.content.Context
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.showly2.common.trakt.TraktSyncWorker
import com.michaldrabik.showly2.common.trakt.quicksync.QuickSyncManager
import com.michaldrabik.showly2.common.trakt.quicksync.QuickSyncWorker
import com.michaldrabik.ui_repository.SettingsRepository
import javax.inject.Inject

@AppScope
class MainTraktCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
  private val quickSyncManager: QuickSyncManager
) {

  suspend fun refreshTraktSyncSchedule(context: Context) {
    if (!settingsRepository.isInitialized()) return
    val schedule = settingsRepository.load().traktSyncSchedule
    TraktSyncWorker.schedule(schedule, context.applicationContext)
  }

  suspend fun refreshTraktQuickSync(context: Context) {
    if (quickSyncManager.isAnyScheduled()) {
      QuickSyncWorker.schedule(context)
    }
  }
}
