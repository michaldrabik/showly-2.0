package com.michaldrabik.showly2.common.trakt

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
import com.michaldrabik.showly2.utilities.extensions.notificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren

abstract class TraktSyncService : Service(), CoroutineScope {

  abstract val tag: String
  abstract val serviceId: String

  abstract val notificationTitleRes: Int
  abstract val notificationProgressRes: Int
  abstract val notificationSuccessRes: Int
  abstract val notificationErrorRes: Int

  override val coroutineContext = Job() + Dispatchers.IO
  protected val runners = mutableListOf<TraktSyncRunner>()

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    Log.d(tag, "Service destroyed.")
    super.onDestroy()
  }

  private fun createBaseNotification() =
    NotificationCompat.Builder(applicationContext, createNotificationChannel())
      .setContentTitle(getString(notificationTitleRes))
      .setSmallIcon(R.drawable.ic_notification)
      .setAutoCancel(true)
      .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))

  protected fun createProgressNotification() = createBaseNotification()
    .setContentText(getString(notificationProgressRes))
    .setCategory(CATEGORY_SERVICE)
    .setOngoing(true)
    .setAutoCancel(false)
    .setProgress(0, 0, true)

  protected fun createSuccessNotification() = createBaseNotification()
    .setContentText(getString(notificationSuccessRes))
    .setPriority(PRIORITY_HIGH)
    .build()

  protected fun createErrorNotification() = createBaseNotification()
    .setContentText(getString(notificationErrorRes))
    .setPriority(PRIORITY_HIGH)
    .build()

  private fun createNotificationChannel(): String {
    if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val id = "$serviceId Service"
      val name = serviceId
      val channel = NotificationChannel(id, name, IMPORTANCE_LOW).apply {
        lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        setSound(null, null)
      }
      notificationManager().createNotificationChannel(channel)
      return id
    }
    return ""
  }

  protected fun clear() = runners.forEach { it.isRunning = false }

  override fun onBind(intent: Intent?): IBinder? = null
}
