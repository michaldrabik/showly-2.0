package com.michaldrabik.showly2.common.trakt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_SERVICE
import androidx.core.content.ContextCompat
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import javax.inject.Inject

class TraktImportService : Service(), CoroutineScope {

  @Inject lateinit var importWatchedRunner: TraktImportWatchedRunner

  override val coroutineContext = Job() + Dispatchers.IO

  override fun onCreate() {
    super.onCreate()
    appComponent().inject(this)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    startForeground(1, createProgressNotification())
    launch {
      try {
        importWatchedRunner.run()
        stopSelf()
      } catch (t: Throwable) {
        stopSelf()
      }
    }
    return START_NOT_STICKY
  }

  private fun createProgressNotification() =
    NotificationCompat.Builder(applicationContext, createNotificationChannel())
      .setContentTitle("Showly Trakt Import")
      .setContentText("Running...")
      .setSmallIcon(R.drawable.ic_notification)
      .setCategory(CATEGORY_SERVICE)
      .setOngoing(true)
      .setProgress(0, 0, true)
      .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
      .build()

  private fun createNotificationChannel(): String {
    if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val id = "Showly Trakt Import Service"
      val name = "Showly Trakt Import"
      val channel = NotificationChannel(id, name, IMPORTANCE_DEFAULT).apply {
        lockscreenVisibility = Notification.VISIBILITY_PRIVATE
      }

      (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).run {
        createNotificationChannel(channel)
      }

      return id
    }
    return ""
  }

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null
}