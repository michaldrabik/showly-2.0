package com.michaldrabik.showly2.common.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bumptech.glide.Glide
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.main.MainActivity
import kotlin.random.Random

class AnnouncementWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

  companion object {
    const val DATA_TITLE = "DATA_TITLE"
    const val DATA_CONTENT = "DATA_CONTENT"
    const val DATA_CHANNEL = "DATA_CHANNEL"
    const val DATA_IMAGE_URL = "DATA_IMAGE_URL"
  }

  override fun doWork(): Result {
    val notification = NotificationCompat.Builder(applicationContext, inputData.getString(DATA_CHANNEL)!!)
      .setContentIntent(createIntent())
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle(inputData.getString(DATA_TITLE))
      .setContentText(inputData.getString(DATA_CONTENT))
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .setColor(ResourcesCompat.getColor(applicationContext.resources, R.color.colorAccent, null))

    val imageUrl = inputData.getString(DATA_IMAGE_URL)
    if ((imageUrl ?: "").isNotBlank()) {
      val target = Glide.with(applicationContext).asBitmap().load(imageUrl).submit()
      try {
        val bitmap = target.get()
        notification.setLargeIcon(bitmap)
      } catch (e: Exception) {
        //NOOP
      } finally {
        Glide.with(applicationContext).clear(target)
      }
    }

    NotificationManagerCompat.from(applicationContext)
      .notify(Random.nextInt(), notification.build())

    return Result.success()
  }

  private fun createIntent(): PendingIntent {
    val notifyIntent = Intent(applicationContext, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    return PendingIntent.getActivity(
      applicationContext, 0, notifyIntent, PendingIntent.FLAG_CANCEL_CURRENT
    )
  }
}