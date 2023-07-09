package com.michaldrabik.ui_settings.sections.notifications.cases

import com.google.firebase.messaging.FirebaseMessaging
import com.michaldrabik.common.ConfigVariant
import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.fcm.NotificationChannel
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_model.NotificationDelay
import com.michaldrabik.ui_model.Settings
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class SettingsNotificationsMainCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val settingsRepository: SettingsRepository,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun getSettings(): Settings = withContext(dispatchers.IO) {
    settingsRepository.load()
  }

  suspend fun enablePushNotifications(enable: Boolean) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(pushNotificationsEnabled = enable)
      settingsRepository.update(new)
    }
    FirebaseMessaging.getInstance().run {
      val suffix = ConfigVariant.FIREBASE_SUFFIX
      if (enable) {
        subscribeToTopic(NotificationChannel.GENERAL_INFO.topicName + suffix)
        subscribeToTopic(NotificationChannel.SHOWS_INFO.topicName + suffix)
      } else {
        unsubscribeFromTopic(NotificationChannel.GENERAL_INFO.topicName + suffix)
        unsubscribeFromTopic(NotificationChannel.SHOWS_INFO.topicName + suffix)
      }
    }
  }

  suspend fun enableAnnouncements(enable: Boolean) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(episodesNotificationsEnabled = enable)
      settingsRepository.update(new)
      announcementManager.refreshShowsAnnouncements()
      announcementManager.refreshMoviesAnnouncements()
    }
  }

  suspend fun setWhenToNotify(delay: NotificationDelay) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(episodesNotificationsDelay = delay)
      settingsRepository.update(new)
      announcementManager.refreshShowsAnnouncements()
    }
  }
}
