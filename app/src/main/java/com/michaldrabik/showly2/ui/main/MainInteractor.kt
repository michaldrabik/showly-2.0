package com.michaldrabik.showly2.ui.main

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.michaldrabik.showly2.BuildConfig
import com.michaldrabik.showly2.common.notifications.AnnouncementManager
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.fcm.NotificationChannel
import com.michaldrabik.showly2.model.Settings
import com.michaldrabik.showly2.model.Tip
import com.michaldrabik.showly2.repository.settings.SettingsRepository
import com.michaldrabik.showly2.repository.tutorial.TipsRepository
import javax.inject.Inject

@AppScope
class MainInteractor @Inject constructor(
  private val settingsRepository: SettingsRepository,
  private val tipsRepository: TipsRepository,
  private val announcementManager: AnnouncementManager
) {

  suspend fun initSettings() {
    if (settingsRepository.isInitialized()) return
    settingsRepository.update(Settings.createInitial())
  }

  suspend fun setInitialRun(value: Boolean) {
    val settings = settingsRepository.load()
    settings.let {
      settingsRepository.update(it.copy(isInitialRun = value))
    }
  }

  suspend fun isInitialRun(): Boolean {
    val settings = settingsRepository.load()
    return settings.isInitialRun
  }

  suspend fun initFcm() {
    val isEnabled = settingsRepository.load().pushNotificationsEnabled
    FirebaseMessaging.getInstance().run {
      val suffix = if (BuildConfig.DEBUG) "-debug" else ""
      if (isEnabled) {
        subscribeToTopic(NotificationChannel.GENERAL_INFO.topicName + suffix)
        subscribeToTopic(NotificationChannel.SHOWS_INFO.topicName + suffix)
      } else {
        unsubscribeFromTopic(NotificationChannel.GENERAL_INFO.topicName + suffix)
        unsubscribeFromTopic(NotificationChannel.SHOWS_INFO.topicName + suffix)
      }
    }
  }

  suspend fun refreshAnnouncements(context: Context) = announcementManager.refreshEpisodesAnnouncements(context)

  fun isTutorialShown(tip: Tip) = when {
    BuildConfig.DEBUG -> true
    else -> tipsRepository.isShown(tip)
  }

  fun setTutorialShown(tip: Tip) = tipsRepository.setShown(tip)
}
