package com.michaldrabik.ui_base.trakt.quicksync

import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.di.UiBaseComponentProvider
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.TraktQuickSyncSuccess
import com.michaldrabik.ui_base.trakt.TraktNotificationsService
import com.michaldrabik.ui_base.utilities.extensions.notificationManager
import com.michaldrabik.ui_repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class QuickSyncService : TraktNotificationsService(), CoroutineScope {

  companion object {
    private const val SYNC_NOTIFICATION_PROGRESS_ID = 916
    private const val SYNC_NOTIFICATION_ERROR_ID = 917

    fun createIntent(context: Context) = Intent(context, QuickSyncService::class.java)
  }

  override val coroutineContext = Job() + Dispatchers.IO

  @Inject lateinit var settingsRepository: SettingsRepository
  @Inject lateinit var quickSyncRunner: QuickSyncRunner

  override fun onCreate() {
    super.onCreate()
    (applicationContext as UiBaseComponentProvider).provideBaseComponent().inject(this)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Timber.d("Service initialized.")

    if (quickSyncRunner.isRunning) {
      Timber.d("Already running. Skipping...")
      return START_NOT_STICKY
    }

    val theme = settingsRepository.theme
    startForeground(SYNC_NOTIFICATION_PROGRESS_ID, createProgressNotification(theme).build())

    Timber.d("Sync started.")
    launch {
      try {
        val count = quickSyncRunner.run()
        if (count > 0) {
          EventsManager.sendEvent(TraktQuickSyncSuccess(count))
          Analytics.logTraktQuickSyncSuccess(count)
        }
      } catch (t: Throwable) {
        notificationManager().notify(
          SYNC_NOTIFICATION_ERROR_ID,
          createErrorNotification(R.string.textTraktQuickSyncError, R.string.textTraktQuickSyncErrorFull)
        )
        Logger.record(t, "Source" to "${QuickSyncService::class.simpleName}")
      } finally {
        Timber.d("Quick Sync completed.")
        clear()
        stopSelf()
      }
    }

    return START_NOT_STICKY
  }

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    super.onDestroy()
  }

  private fun clear() {
    quickSyncRunner.isRunning = false
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
