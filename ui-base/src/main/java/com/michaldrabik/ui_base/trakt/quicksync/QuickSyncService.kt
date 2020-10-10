package com.michaldrabik.ui_base.trakt.quicksync

import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.di.UiBaseComponentProvider
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.TraktQuickSyncSuccess
import com.michaldrabik.ui_base.trakt.TraktNotificationsService
import com.michaldrabik.ui_base.utilities.extensions.notificationManager
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class QuickSyncService : TraktNotificationsService(), CoroutineScope {

  companion object {
    private const val SYNC_NOTIFICATION_PROGRESS_ID = 916
    private const val SYNC_NOTIFICATION_ERROR_ID = 917

    fun createIntent(context: Context) = Intent(context, QuickSyncService::class.java)
  }

  override val coroutineContext = Job() + Dispatchers.IO

  @Inject
  lateinit var quickSyncRunner: QuickSyncRunner

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
    startForeground(SYNC_NOTIFICATION_PROGRESS_ID, createProgressNotification().build())

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
        val exception = Throwable(QuickSyncService::class.simpleName, t)
        FirebaseCrashlytics.getInstance().recordException(exception)
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
