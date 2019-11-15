package com.michaldrabik.showly2.ui.settings

import com.google.firebase.messaging.FirebaseMessaging
import com.michaldrabik.showly2.BuildConfig
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.fcm.FcmChannel
import com.michaldrabik.showly2.model.Settings
import com.michaldrabik.showly2.repository.settings.SettingsRepository
import javax.inject.Inject

@AppScope
class SettingsInteractor @Inject constructor(
  private val settingsRepository: SettingsRepository
) {

  suspend fun getSettings(): Settings = settingsRepository.load()!!

  suspend fun setRecentShowsAmount(amount: Int) {
    check(amount in Config.MY_SHOWS_RECENTS_OPTIONS)
    val settings = settingsRepository.load()
    settings?.let {
      val new = it.copy(myShowsRecentsAmount = amount)
      settingsRepository.update(new)
    }
  }

  suspend fun enablePushNotifications(enable: Boolean) {
    val settings = settingsRepository.load()
    settings?.let {
      val new = it.copy(pushNotificationsEnabled = enable)
      settingsRepository.update(new)
    }
    FirebaseMessaging.getInstance().run {
      val suffix = if (BuildConfig.DEBUG) "-debug" else ""
      if (enable) {
        subscribeToTopic(FcmChannel.GENERAL_INFO.topicName + suffix)
        subscribeToTopic(FcmChannel.SHOWS_INFO.topicName + suffix)
      } else {
        unsubscribeFromTopic(FcmChannel.GENERAL_INFO.topicName + suffix)
        unsubscribeFromTopic(FcmChannel.SHOWS_INFO.topicName + suffix)
      }
    }
  }

  suspend fun enableShowsNotifications(enable: Boolean) {
    val settings = settingsRepository.load()
    settings?.let {
      val new = it.copy(showsNotificationsEnabled = enable)
      settingsRepository.update(new)
    }
  }
}