package com.michaldrabik.showly2.ui.main

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.michaldrabik.showly2.BuildConfig
import com.michaldrabik.showly2.Config.MY_SHOWS_RECENTS_DEFAULT
import com.michaldrabik.showly2.common.notifications.AnnouncementManager
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.fcm.NotificationChannel
import com.michaldrabik.showly2.model.NotificationDelay.WHEN_AVAILABLE
import com.michaldrabik.showly2.model.Settings
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.repository.settings.SettingsRepository
import com.michaldrabik.showly2.ui.UiCache
import javax.inject.Inject

@AppScope
class MainInteractor @Inject constructor(
  private val settingsRepository: SettingsRepository,
  private val announcementManager: AnnouncementManager,
  private val uiCache: UiCache
) {

  suspend fun initSettings() {
    val settings = settingsRepository.load()
    if (settings == null) {
      val newSettings = Settings(
        isInitialRun = true,
        pushNotificationsEnabled = true,
        episodesNotificationsEnabled = true,
        episodesNotificationsDelay = WHEN_AVAILABLE,
        myShowsEndedSortBy = SortOrder.NAME,
        myShowsIncomingSortBy = SortOrder.NAME,
        myShowsRunningSortBy = SortOrder.NAME,
        myShowsRecentsAmount = MY_SHOWS_RECENTS_DEFAULT
      )
      settingsRepository.update(newSettings)
    }
  }

  suspend fun setInitialRun(value: Boolean) {
    val settings = settingsRepository.load()
    settings?.let {
      settingsRepository.update(it.copy(isInitialRun = value))
    }
  }

  suspend fun isInitialRun(): Boolean {
    val settings = settingsRepository.load()
    return settings?.isInitialRun ?: true
  }

  suspend fun initFcm() {
    val isEnabled = settingsRepository.load()?.pushNotificationsEnabled ?: false
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

  fun clearCache() = uiCache.clear()
}