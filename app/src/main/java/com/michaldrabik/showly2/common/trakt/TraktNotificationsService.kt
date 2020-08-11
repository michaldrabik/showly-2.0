package com.michaldrabik.showly2.common.trakt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.Service
import android.os.Build
import android.os.Build.VERSION
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_SERVICE
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.content.ContextCompat
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.extensions.notificationManager

abstract class TraktNotificationsService : Service() {

  private fun createBaseNotification() =
    NotificationCompat.Builder(applicationContext, createNotificationChannel())
      .setContentTitle(getString(R.string.textTraktSync))
      .setSmallIcon(R.drawable.ic_notification)
      .setAutoCancel(true)
      .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))

  protected fun createProgressNotification() = createBaseNotification()
    .setContentText(getString(R.string.textTraktSyncRunning))
    .setCategory(CATEGORY_SERVICE)
    .setOngoing(true)
    .setAutoCancel(false)
    .setProgress(0, 0, true)

  protected fun createSuccessNotification() = createBaseNotification()
    .setContentText(getString(R.string.textTraktSyncComplete))
    .setPriority(PRIORITY_HIGH)
    .build()

  protected fun createErrorNotification(@StringRes titleTextRes: Int, @StringRes bigTextRes: Int? = null) = createBaseNotification()
    .setContentText(getString(titleTextRes))
    .apply {
      bigTextRes?.let {
        setStyle(NotificationCompat.BigTextStyle().bigText(getString(bigTextRes)))
      }
    }
    .setPriority(PRIORITY_HIGH)
    .build()

  private fun createNotificationChannel(): String {
    if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val id = "Showly Trakt Sync Service"
      val name = "Showly Trakt Sync"
      val channel = NotificationChannel(id, name, IMPORTANCE_LOW).apply {
        lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        setSound(null, null)
      }
      notificationManager().createNotificationChannel(channel)
      return id
    }
    return ""
  }
}
