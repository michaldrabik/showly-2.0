package com.michaldrabik.showly2.common.trakt.exports

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_SERVICE
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.content.ContextCompat
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.common.events.EventsManager
import com.michaldrabik.showly2.common.events.TraktExportError
import com.michaldrabik.showly2.common.events.TraktExportStart
import com.michaldrabik.showly2.common.events.TraktExportSuccess
import com.michaldrabik.showly2.utilities.extensions.notificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import javax.inject.Inject

class TraktExportService : Service(), CoroutineScope {

  companion object {
    private const val TAG = "TraktExportService"

    private const val EXPORT_NOTIFICATION_PROGRESS_ID = 1126
    private const val EXPORT_NOTIFICATION_COMPLETE_SUCCESS_ID = 1127
    private const val EXPORT_NOTIFICATION_COMPLETE_ERROR_ID = 1128
  }

  @Inject lateinit var exportWatchlistRunner: TraktExportWatchlistRunner

  override val coroutineContext = Job() + Dispatchers.IO

  override fun onCreate() {
    super.onCreate()
    appComponent().inject(this)
  }

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    Log.d(TAG, "Service destroyed.")
    super.onDestroy()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.d(TAG, "Service initialized.")
    if (exportWatchlistRunner.isRunning) {
      Log.d(TAG, "Already running. Skipping...")
      return START_NOT_STICKY
    }
    startForeground(EXPORT_NOTIFICATION_PROGRESS_ID, createProgressNotification().build())

    Log.d(TAG, "Export started.")
    launch {
      try {
        EventsManager.sendEvent(TraktExportStart)
        runExportWatchlist()
        notificationManager().notify(EXPORT_NOTIFICATION_COMPLETE_SUCCESS_ID, createSuccessNotification())
        EventsManager.sendEvent(TraktExportSuccess)
      } catch (t: Throwable) {
        notificationManager().notify(EXPORT_NOTIFICATION_COMPLETE_ERROR_ID, createErrorNotification())
        EventsManager.sendEvent(TraktExportError)
      } finally {
        clear()
        stopSelf()
      }
    }
    Log.d(TAG, "Export completed.")

    return START_NOT_STICKY
  }

  private suspend fun runExportWatchlist() {
//    exportWatchlistRunner.progressListener = { show: Show, progress: Int, total: Int ->
//      val notification = createProgressNotification().run {
//        setContentText("Processing \'${show.title}\'...")
//        setProgress(totalProgress + total, totalProgress + progress, false)
//      }
//      notificationManager().notify(IMPORT_NOTIFICATION_PROGRESS_ID, notification.build())
//      EventsManager.sendEvent(TraktExportProgress)
//    }
    exportWatchlistRunner.run()
  }

  private fun createBaseNotification() =
    NotificationCompat.Builder(applicationContext, createNotificationChannel())
      .setContentTitle(getString(R.string.textTraktExport))
      .setSmallIcon(R.drawable.ic_notification)
      .setAutoCancel(true)
      .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))

  private fun createProgressNotification() = createBaseNotification()
    .setContentText(getString(R.string.textTraktExportRunning))
    .setCategory(CATEGORY_SERVICE)
    .setOngoing(true)
    .setAutoCancel(false)
    .setProgress(0, 0, true)

  private fun createSuccessNotification() = createBaseNotification()
    .setContentText(getString(R.string.textTraktExportComplete))
    .setPriority(PRIORITY_HIGH)
    .build()

  private fun createErrorNotification() = createBaseNotification()
    .setContentText(getString(R.string.textTraktExportError))
    .setPriority(PRIORITY_HIGH)
    .build()

  private fun createNotificationChannel(): String {
    if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val id = "Showly Trakt Export Service"
      val name = "Showly Trakt Export"
      val channel = NotificationChannel(id, name, IMPORTANCE_LOW).apply {
        lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        setSound(null, null)
      }
      notificationManager().createNotificationChannel(channel)
      return id
    }
    return ""
  }

  private fun clear() {
    exportWatchlistRunner.isRunning = false
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
