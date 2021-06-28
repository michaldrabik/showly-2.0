package com.michaldrabik.ui_base.fcm

import androidx.core.app.NotificationManagerCompat

enum class NotificationChannel(
  val displayName: String,
  val description: String,
  val importance: Int,
  val topicName: String
) {
  GENERAL_INFO(
    "General Info",
    "General information and announcements",
    NotificationManagerCompat.IMPORTANCE_HIGH,
    "general"
  ),
  SHOWS_INFO(
    "Shows Info",
    "Shows related information",
    NotificationManagerCompat.IMPORTANCE_DEFAULT,
    "shows"
  ),
  EPISODES_ANNOUNCEMENTS(
    "Episodes Announcements",
    "Episodes and seasons announcements",
    NotificationManagerCompat.IMPORTANCE_DEFAULT,
    "shows_announcements"
  ),
  MOVIES_ANNOUNCEMENTS(
    "Movies Announcements",
    "Movies announcements",
    NotificationManagerCompat.IMPORTANCE_DEFAULT,
    "movies_announcements"
  )
}
