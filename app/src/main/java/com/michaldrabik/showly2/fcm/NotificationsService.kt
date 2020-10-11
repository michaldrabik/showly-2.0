package com.michaldrabik.showly2.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class NotificationsService : FirebaseMessagingService() {

  override fun onMessageReceived(message: RemoteMessage) {
    super.onMessageReceived(message)
    Timber.d("onMessageReceived: $message")
  }

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Timber.d("onNewToken: $token")
  }
}
