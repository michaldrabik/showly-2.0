package com.michaldrabik.showly2.common.trakt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_SERVICE
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import javax.inject.Inject

class TraktImportService : Service(), CoroutineScope {

  companion object {
    private const val IMPORT_NOTIFICATION_PROGRESS_ID = 826
    private const val IMPORT_NOTIFICATION_COMPLETE_ID = 827

    private const val ACTION_IMPORT_PROGRESS = "ACTION_IMPORT_PROGRESS"
    private const val ACTION_IMPORT_COMPLETE = "ACTION_IMPORT_COMPLETE"
  }

  @Inject lateinit var importWatchedRunner: TraktImportWatchedRunner

  override val coroutineContext = Job() + Dispatchers.IO

  override fun onCreate() {
    super.onCreate()
    appComponent().inject(this)
  }

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    super.onDestroy()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    startForeground(IMPORT_NOTIFICATION_PROGRESS_ID, createProgressNotification().build())
    launch {
      try {
        importWatchedRunner.run {
          progressListener = { show: Show, progress: Int, total: Int ->
            val notification = createProgressNotification().run {
              setContentText("Processing \'${show.title}\'...")
              setProgress(total, progress, false)
            }
            notificationsManager().notify(IMPORT_NOTIFICATION_PROGRESS_ID, notification.build())
          }
          run()
          notifyBroadcast(ACTION_IMPORT_PROGRESS)
        }
        notificationsManager().notify(IMPORT_NOTIFICATION_COMPLETE_ID, createSuccessNotification())
        notifyBroadcast(ACTION_IMPORT_COMPLETE)
        stopSelf()
      } catch (t: Throwable) {
        notificationsManager().notify(IMPORT_NOTIFICATION_COMPLETE_ID, createErrorNotification())
        notifyBroadcast(ACTION_IMPORT_COMPLETE)
        stopSelf()
      }
    }
    return START_NOT_STICKY
  }

  private fun createProgressNotification() =
    NotificationCompat.Builder(applicationContext, createNotificationChannel())
      .setContentTitle(getString(R.string.textTraktImport))
      .setContentText(getString(R.string.textTraktImportRunning))
      .setSmallIcon(R.drawable.ic_notification)
      .setCategory(CATEGORY_SERVICE)
      .setOngoing(true)
      .setProgress(0, 0, true)
      .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))

  private fun createSuccessNotification() =
    NotificationCompat.Builder(applicationContext, createNotificationChannel())
      .setContentTitle(getString(R.string.textTraktImport))
      .setContentText(getString(R.string.textTraktImportComplete))
      .setSmallIcon(R.drawable.ic_notification)
      .setAutoCancel(true)
      .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
      .build()

  private fun createErrorNotification() =
    NotificationCompat.Builder(applicationContext, createNotificationChannel())
      .setContentTitle(getString(R.string.textTraktImport))
      .setContentText(getString(R.string.textTraktImportError))
      .setSmallIcon(R.drawable.ic_notification)
      .setAutoCancel(true)
      .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
      .build()

  private fun createNotificationChannel(): String {
    if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val id = "Showly Trakt Import Service"
      val name = "Showly Trakt Import"
      val channel = NotificationChannel(id, name, IMPORTANCE_LOW).apply {
        lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        setSound(null, null)
      }
      notificationsManager().createNotificationChannel(channel)
      return id
    }
    return ""
  }

  private fun notifyBroadcast(action: String) {
    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(action))
  }

  private fun notificationsManager() = (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

  override fun onBind(intent: Intent?): IBinder? = null
}