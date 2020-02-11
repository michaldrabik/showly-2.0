package com.michaldrabik.showly2.common.trakt.imports

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
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.common.events.EventsManager
import com.michaldrabik.showly2.common.events.TraktImportAuthError
import com.michaldrabik.showly2.common.events.TraktImportError
import com.michaldrabik.showly2.common.events.TraktImportProgress
import com.michaldrabik.showly2.common.events.TraktImportStart
import com.michaldrabik.showly2.common.events.TraktImportSuccess
import com.michaldrabik.showly2.model.error.TraktAuthError
import com.michaldrabik.showly2.utilities.extensions.notificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import javax.inject.Inject

class TraktImportService : Service(), CoroutineScope {

  companion object {
    private const val TAG = "TraktImportService"

    private const val IMPORT_NOTIFICATION_PROGRESS_ID = 826
    private const val IMPORT_NOTIFICATION_COMPLETE_SUCCESS_ID = 827
    private const val IMPORT_NOTIFICATION_COMPLETE_ERROR_ID = 828
  }

  @Inject lateinit var importWatchedRunner: TraktImportWatchedRunner
  @Inject lateinit var importWatchlistRunner: TraktImportWatchlistRunner

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
    if (importWatchedRunner.isRunning || importWatchlistRunner.isRunning) {
      Log.d(TAG, "Already running. Skipping...")
      return START_NOT_STICKY
    }
    startForeground(IMPORT_NOTIFICATION_PROGRESS_ID, createProgressNotification().build())

    Log.d(TAG, "Import started.")
    launch {
      try {
        EventsManager.sendEvent(TraktImportStart)

        val resultCount = runImportWatched()
        runImportWatchlist(resultCount)

        notificationManager().notify(IMPORT_NOTIFICATION_COMPLETE_SUCCESS_ID, createSuccessNotification())
        EventsManager.sendEvent(TraktImportSuccess)
      } catch (t: Throwable) {
        if (t is TraktAuthError) EventsManager.sendEvent(TraktImportAuthError)
        notificationManager().notify(IMPORT_NOTIFICATION_COMPLETE_ERROR_ID, createErrorNotification())
        EventsManager.sendEvent(TraktImportError)
      } finally {
        Log.d(TAG, "Import completed.")
        notificationManager().cancel(IMPORT_NOTIFICATION_PROGRESS_ID)
        clear()
        stopSelf()
      }
    }
    return START_NOT_STICKY
  }

  private suspend fun runImportWatched(): Int {
    importWatchedRunner.progressListener = { show: Show, progress: Int, total: Int ->
      val notification = createProgressNotification().run {
        setContentText("Processing \'${show.title}\'...")
        setProgress(total, progress, false)
      }
      notificationManager().notify(IMPORT_NOTIFICATION_PROGRESS_ID, notification.build())
      EventsManager.sendEvent(TraktImportProgress)
    }
    return importWatchedRunner.run()
  }

  private suspend fun runImportWatchlist(totalProgress: Int) {
    importWatchlistRunner.progressListener = { show: Show, progress: Int, total: Int ->
      val notification = createProgressNotification().run {
        setContentText("Processing \'${show.title}\'...")
        setProgress(totalProgress + total, totalProgress + progress, false)
      }
      notificationManager().notify(IMPORT_NOTIFICATION_PROGRESS_ID, notification.build())
      EventsManager.sendEvent(TraktImportProgress)
    }
    importWatchlistRunner.run()
  }

  private fun createBaseNotification() =
    NotificationCompat.Builder(applicationContext, createNotificationChannel())
      .setContentTitle(getString(R.string.textTraktImport))
      .setSmallIcon(R.drawable.ic_notification)
      .setAutoCancel(true)
      .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))

  private fun createProgressNotification() = createBaseNotification()
    .setContentText(getString(R.string.textTraktImportRunning))
    .setCategory(CATEGORY_SERVICE)
    .setOngoing(true)
    .setAutoCancel(false)
    .setProgress(0, 0, true)

  private fun createSuccessNotification() = createBaseNotification()
    .setContentText(getString(R.string.textTraktImportComplete))
    .setPriority(PRIORITY_HIGH)
    .build()

  private fun createErrorNotification() = createBaseNotification()
    .setContentText(getString(R.string.textTraktImportError))
    .setPriority(PRIORITY_HIGH)
    .build()

  private fun createNotificationChannel(): String {
    if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val id = "Showly Trakt Import Service"
      val name = "Showly Trakt Import"
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
    importWatchlistRunner.isRunning = false
    importWatchedRunner.isRunning = false
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
