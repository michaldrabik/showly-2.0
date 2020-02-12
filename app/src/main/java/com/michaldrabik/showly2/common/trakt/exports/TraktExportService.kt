package com.michaldrabik.showly2.common.trakt.exports

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.common.events.EventsManager
import com.michaldrabik.showly2.common.events.TraktExportError
import com.michaldrabik.showly2.common.events.TraktExportStart
import com.michaldrabik.showly2.common.events.TraktExportSuccess
import com.michaldrabik.showly2.common.trakt.TraktSyncService
import com.michaldrabik.showly2.utilities.extensions.notificationManager
import kotlinx.coroutines.launch
import javax.inject.Inject

class TraktExportService : TraktSyncService() {

  companion object {
    private const val EXPORT_NOTIFICATION_PROGRESS_ID = 1126
    private const val EXPORT_NOTIFICATION_COMPLETE_SUCCESS_ID = 1127
    private const val EXPORT_NOTIFICATION_COMPLETE_ERROR_ID = 1128
  }

  @Inject lateinit var exportWatchedRunner: TraktExportWatchedRunner
  @Inject lateinit var exportWatchlistRunner: TraktExportWatchlistRunner

  override val tag = "TraktExportService"
  override val serviceId = "Showly Trakt Export"

  override val notificationTitleRes = R.string.textTraktExport
  override val notificationProgressRes = R.string.textTraktExportRunning
  override val notificationSuccessRes = R.string.textTraktExportComplete
  override val notificationErrorRes = R.string.textTraktExportError

  override fun onCreate() {
    super.onCreate()
    appComponent().inject(this)
    runners.addAll(listOf(exportWatchedRunner, exportWatchlistRunner))
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.d(tag, "Service initialized.")
    if (runners.any { it.isRunning }) {
      Log.d(tag, "Already running. Skipping...")
      return START_NOT_STICKY
    }
    startForeground(EXPORT_NOTIFICATION_PROGRESS_ID, createProgressNotification().build())

    Log.d(tag, "Export started.")
    launch {
      try {
        EventsManager.sendEvent(TraktExportStart)
        exportWatchedRunner.run()
        exportWatchlistRunner.run()
        EventsManager.sendEvent(TraktExportSuccess)
        notificationManager().notify(EXPORT_NOTIFICATION_COMPLETE_SUCCESS_ID, createSuccessNotification())
      } catch (t: Throwable) {
        EventsManager.sendEvent(TraktExportError)
        notificationManager().notify(EXPORT_NOTIFICATION_COMPLETE_ERROR_ID, createErrorNotification())
      } finally {
        Log.d(tag, "Export completed.")
        notificationManager().cancel(EXPORT_NOTIFICATION_PROGRESS_ID)
        clear()
        stopSelf()
      }
    }
    return START_NOT_STICKY
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
