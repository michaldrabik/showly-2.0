package com.michaldrabik.showly2.ui.main.cases

import android.content.Context
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.ui_base.trakt.TraktSyncWorker
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncWorker
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MainTraktCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
  private val quickSyncManager: QuickSyncManager,
) {

  suspend fun refreshTraktSyncSchedule(context: Context) {
    if (!settingsRepository.isInitialized()) return
    val schedule = settingsRepository.load().traktSyncSchedule
    TraktSyncWorker.schedule(context, schedule, cancelExisting = false)
  }

  suspend fun refreshTraktQuickSync(context: Context) {
    if (quickSyncManager.isAnyScheduled()) {
      QuickSyncWorker.schedule(context)
    }
  }
}
