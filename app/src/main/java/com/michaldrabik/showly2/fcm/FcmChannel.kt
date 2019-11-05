package com.michaldrabik.showly2.fcm

import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
enum class FcmChannel(
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
    "Shows related information and announcements",
    NotificationManager.IMPORTANCE_DEFAULT,
    "shows"
  ),
}