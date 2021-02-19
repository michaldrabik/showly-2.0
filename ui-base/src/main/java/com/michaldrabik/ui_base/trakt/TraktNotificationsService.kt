package com.michaldrabik.ui_base.trakt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.Service
import android.app.UiModeManager.MODE_NIGHT_NO
import android.app.UiModeManager.MODE_NIGHT_YES
import android.os.Build
import android.os.Build.VERSION
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_SERVICE
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.content.ContextCompat
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.utilities.extensions.notificationManager
import java.util.concurrent.TimeUnit

abstract class TraktNotificationsService : Service() {

  private fun createBaseNotification(theme: Int): NotificationCompat.Builder {
    val color = when (theme) {
      MODE_NIGHT_YES -> R.color.colorNotificationDark
      MODE_NIGHT_NO -> R.color.colorNotificationLight
      else -> R.color.colorNotificationDark
    }
    return NotificationCompat.Builder(applicationContext, createNotificationChannel())
      .setContentTitle(getString(R.string.textTraktSync))
      .setSmallIcon(R.drawable.ic_notification)
      .setAutoCancel(true)
      .setColor(ContextCompat.getColor(applicationContext, color))
  }

  protected fun createProgressNotification(theme: Int) =
    createBaseNotification(theme)
      .setContentText(getString(R.string.textTraktSyncRunning))
      .setCategory(CATEGORY_SERVICE)
      .setOngoing(true)
      .setAutoCancel(false)
      .setProgress(0, 0, true)

  protected fun createSuccessNotification(theme: Int) =
    createBaseNotification(theme)
      .setTimeoutAfter(TimeUnit.SECONDS.toMillis(5))
      .setContentText(getString(R.string.textTraktSyncComplete))
      .setPriority(PRIORITY_HIGH)
      .build()

  protected fun createErrorNotification(
    theme: Int,
    @StringRes titleTextRes: Int,
    @StringRes bigTextRes: Int? = null
  ) = createBaseNotification(theme)
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
