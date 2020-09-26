package com.michaldrabik.showly2.ui.settings.cases

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.showly2.BuildConfig
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.common.images.ShowImagesProvider
import com.michaldrabik.showly2.common.notifications.AnnouncementManager
import com.michaldrabik.showly2.fcm.NotificationChannel
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.MyShowsSection.FINISHED
import com.michaldrabik.ui_model.MyShowsSection.RECENTS
import com.michaldrabik.ui_model.MyShowsSection.UPCOMING
import com.michaldrabik.ui_model.MyShowsSection.WATCHING
import com.michaldrabik.ui_model.NotificationDelay
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_repository.SettingsRepository
import javax.inject.Inject

@AppScope
class SettingsMainCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
  private val announcementManager: AnnouncementManager,
  private val imagesProvider: ShowImagesProvider
) {

  suspend fun getSettings(): Settings = settingsRepository.load()

  suspend fun setRecentShowsAmount(amount: Int) {
    check(amount in Config.MY_SHOWS_RECENTS_OPTIONS)
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(myShowsRecentsAmount = amount)
      settingsRepository.update(new)
    }
  }

  suspend fun enablePushNotifications(enable: Boolean) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(pushNotificationsEnabled = enable)
      settingsRepository.update(new)
    }
    FirebaseMessaging.getInstance().run {
      val suffix = if (BuildConfig.DEBUG) "-debug" else ""
      if (enable) {
        subscribeToTopic(NotificationChannel.GENERAL_INFO.topicName + suffix)
        subscribeToTopic(NotificationChannel.SHOWS_INFO.topicName + suffix)
      } else {
        unsubscribeFromTopic(NotificationChannel.GENERAL_INFO.topicName + suffix)
        unsubscribeFromTopic(NotificationChannel.SHOWS_INFO.topicName + suffix)
      }
    }
  }

  suspend fun enableEpisodesAnnouncements(enable: Boolean, context: Context) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(episodesNotificationsEnabled = enable)
      settingsRepository.update(new)
      announcementManager.refreshEpisodesAnnouncements(context.applicationContext)
    }
  }

  suspend fun enableMyShowsSection(section: MyShowsSection, isEnabled: Boolean) {
    val settings = settingsRepository.load()
    settings.let {
      val new = when (section) {
        RECENTS -> it.copy(myShowsRecentIsEnabled = isEnabled)
        WATCHING -> it.copy(myShowsRunningIsEnabled = isEnabled)
        FINISHED -> it.copy(myShowsEndedIsEnabled = isEnabled)
        UPCOMING -> it.copy(myShowsIncomingIsEnabled = isEnabled)
        else -> error("Should not be used here.")
      }
      settingsRepository.update(new)
    }
  }

  suspend fun enableArchivedStatistics(enable: Boolean) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(archiveShowsIncludeStatistics = enable)
      settingsRepository.update(new)
    }
  }

  suspend fun setWhenToNotify(delay: NotificationDelay, context: Context) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(episodesNotificationsDelay = delay)
      settingsRepository.update(new)
      announcementManager.refreshEpisodesAnnouncements(context.applicationContext)
    }
  }

  suspend fun deleteImagesCache() = imagesProvider.deleteLocalCache()
}
