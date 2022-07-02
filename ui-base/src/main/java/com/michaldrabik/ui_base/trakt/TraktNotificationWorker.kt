package com.michaldrabik.ui_base.trakt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.utilities.extensions.notificationManager
import java.util.concurrent.TimeUnit

abstract class TraktNotificationWorker constructor(
  val context: Context,
  workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

  private fun createBaseNotification(theme: Int): NotificationCompat.Builder {
    val color = when (theme) {
      UiModeManager.MODE_NIGHT_YES -> R.color.colorNotificationDark
      UiModeManager.MODE_NIGHT_NO -> R.color.colorNotificationLight
      else -> R.color.colorNotificationDark
    }
    return NotificationCompat.Builder(applicationContext, createNotificationChannel())
      .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
      .setContentTitle(context.getString(R.string.textTraktSync))
      .setSmallIcon(R.drawable.ic_notification)
      .setAutoCancel(true)
      .setColor(ContextCompat.getColor(applicationContext, color))
  }

  protected fun createProgressNotification(
    theme: Int,
    content: String?,
    maxProgress: Int,
    progress: Int,
    isIntermediate: Boolean
  ): Notification =
    createBaseNotification(theme)
      .setContentText(content ?: context.getString(R.string.textTraktSyncRunning))
      .setCategory(NotificationCompat.CATEGORY_SERVICE)
      .setOngoing(true)
      .setAutoCancel(false)
      .setProgress(maxProgress, progress, isIntermediate)
      .build()

  protected fun createSuccessNotification(theme: Int): Notification =
    createBaseNotification(theme)
      .setTimeoutAfter(TimeUnit.SECONDS.toMillis(3))
      .setContentText(context.getString(R.string.textTraktSyncComplete))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .build()

  protected fun createErrorNotification(
    theme: Int,
    @StringRes titleTextRes: Int,
    @StringRes bigTextRes: Int,
    action: NotificationCompat.Action? = null
  ): Notification =
    createBaseNotification(theme)
      .setContentTitle(context.getString(titleTextRes))
      .setContentText(context.getString(bigTextRes))
      .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(bigTextRes)))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .apply { action?.let { addAction(it) } }
      .build()

  private fun createNotificationChannel(): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val id = "Showly Trakt Sync Service"
      val name = "Showly Trakt Sync"
      val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW).apply {
        lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        setSound(null, null)
      }
      applicationContext.notificationManager().createNotificationChannel(channel)
      return id
    }
    return ""
  }
}
