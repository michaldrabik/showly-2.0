package com.michaldrabik.showly2.ui.main.cases

import android.content.Context
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_base.trakt.TraktSyncWorker
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncWorker
import javax.inject.Inject

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
