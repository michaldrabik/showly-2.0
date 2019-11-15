package com.michaldrabik.showly2.ui.main

import com.google.firebase.messaging.FirebaseMessaging
import com.michaldrabik.showly2.BuildConfig
import com.michaldrabik.showly2.Config.MY_SHOWS_RECENTS_DEFAULT
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.fcm.FcmChannel
import com.michaldrabik.showly2.model.Settings
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.repository.settings.SettingsRepository
import com.michaldrabik.showly2.ui.UiCache
import javax.inject.Inject

@AppScope
class MainInteractor @Inject constructor(
  private val settingsRepository: SettingsRepository,
  private val uiCache: UiCache
) {

  suspend fun initSettings() {
    val settings = settingsRepository.load()
    if (settings == null) {
      val newSettings = Settings(
        isInitialRun = true,
        pushNotificationsEnabled = true,
        showsNotificationsEnabled = true,
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
        subscribeToTopic(FcmChannel.GENERAL_INFO.topicName + suffix)
        subscribeToTopic(FcmChannel.SHOWS_INFO.topicName + suffix)
      } else {
        unsubscribeFromTopic(FcmChannel.GENERAL_INFO.topicName + suffix)
        unsubscribeFromTopic(FcmChannel.SHOWS_INFO.topicName + suffix)
      }
    }
  }

  fun clearCache() = uiCache.clear()
}