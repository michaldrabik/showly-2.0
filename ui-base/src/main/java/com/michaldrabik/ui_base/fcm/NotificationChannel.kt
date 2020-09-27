package com.michaldrabik.ui_base.fcm

import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
enum class NotificationChannel(
  val displayName: String,
  val description: String,
  val importance: Int,
  val topicName: String
) {
  GENERAL_INFO(
    "General Info",
    "General information and announcements",
    NotificationManager.IMPORTANCE_HIGH,
    "general"
  ),
  SHOWS_INFO(
    "Shows Info",
    "Shows related information",
    NotificationManager.IMPORTANCE_DEFAULT,
    "shows"
  ),
  EPISODES_ANNOUNCEMENTS(
    "Episodes Announcements",
    "Episodes and seasons announcements",
    NotificationManager.IMPORTANCE_DEFAULT,
    "shows_announcements"
  ),
}
