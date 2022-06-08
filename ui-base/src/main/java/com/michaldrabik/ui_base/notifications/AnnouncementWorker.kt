package com.michaldrabik.ui_base.notifications

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.UiModeManager.MODE_NIGHT_NO
import android.app.UiModeManager.MODE_NIGHT_YES
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bumptech.glide.Glide
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.R
import kotlin.random.Random

class AnnouncementWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

  companion object {
    const val DATA_SHOW_ID = "DATA_SHOW_ID"
    const val DATA_MOVIE_ID = "DATA_MOVIE_ID"
    const val DATA_TITLE = "DATA_TITLE"
    const val DATA_CONTENT = "DATA_CONTENT"
    const val DATA_CHANNEL = "DATA_CHANNEL"
    const val DATA_IMAGE_URL = "DATA_IMAGE_URL"
    const val DATA_THEME = "DATA_THEME"
  }

  override fun doWork(): Result {
    val color = when (inputData.getInt(DATA_THEME, MODE_NIGHT_YES)) {
      MODE_NIGHT_YES -> R.color.colorNotificationDark
      MODE_NIGHT_NO -> R.color.colorNotificationLight
      else -> R.color.colorNotificationDark
    }

    val notification = NotificationCompat.Builder(applicationContext, inputData.getString(DATA_CHANNEL)!!)
      .setContentIntent(createIntent())
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle(inputData.getString(DATA_TITLE))
      .setContentText(inputData.getString(DATA_CONTENT))
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .setColor(ContextCompat.getColor(applicationContext, color))

    val imageUrl = inputData.getString(DATA_IMAGE_URL)
    if ((imageUrl ?: "").isNotBlank()) {
      val target = Glide.with(applicationContext).asBitmap().load(imageUrl).submit()
      try {
        val bitmap = target.get()
        notification.setLargeIcon(bitmap)
      } catch (e: Exception) {
        // NOOP
      } finally {
        Glide.with(applicationContext).clear(target)
      }
    }

    NotificationManagerCompat.from(applicationContext)
      .notify(Random.nextInt(), notification.build())

    return Result.success()
  }

  private fun createIntent(): PendingIntent {
    var requestCode = 0L
    val targetClass = Class.forName(Config.HOST_ACTIVITY_NAME)
    val notifyIntent = Intent(applicationContext, targetClass).apply {
      val showId = inputData.getLong(DATA_SHOW_ID, -1)
      val movieId = inputData.getLong(DATA_MOVIE_ID, -1)
      when {
        showId != -1L -> {
          putExtra("EXTRA_SHOW_ID", showId.toString())
          requestCode = showId
        }
        movieId != -1L -> {
          putExtra("EXTRA_MOVIE_ID", movieId.toString())
          requestCode = movieId
        }
      }
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    return PendingIntent.getActivity(
      applicationContext, requestCode.toInt(), notifyIntent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
    )
  }
}
