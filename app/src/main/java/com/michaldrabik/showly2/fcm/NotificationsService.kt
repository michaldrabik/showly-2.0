package com.michaldrabik.showly2.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationsService : FirebaseMessagingService() {

  override fun onMessageReceived(message: RemoteMessage) {
    super.onMessageReceived(message)
    Log.d("NotificationsService", "onMessageReceived: $message")
  }
}
