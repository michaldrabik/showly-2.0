package com.michaldrabik.showly2.ui.main

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.messaging.FirebaseMessaging
import com.michaldrabik.showly2.BuildConfig.DEBUG
import com.michaldrabik.showly2.BuildConfig.VERSION_CODE
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.common.notifications.AnnouncementManager
import com.michaldrabik.showly2.common.trakt.TraktSyncWorker
import com.michaldrabik.showly2.common.trakt.quicksync.QuickSyncManager
import com.michaldrabik.showly2.common.trakt.quicksync.QuickSyncWorker
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.fcm.NotificationChannel
import com.michaldrabik.showly2.model.Settings
import com.michaldrabik.showly2.model.Tip
import com.michaldrabik.showly2.repository.rating.RatingsRepository
import com.michaldrabik.showly2.repository.settings.SettingsRepository
import com.michaldrabik.showly2.repository.tutorial.TipsRepository
import javax.inject.Inject
import javax.inject.Named

@AppScope
class MainInteractor @Inject constructor(
  private val settingsRepository: SettingsRepository,
  private val tipsRepository: TipsRepository,
  private val ratingsRepository: RatingsRepository,
  private val quickSyncManager: QuickSyncManager,
  private val announcementManager: AnnouncementManager,
  @Named("miscPreferences") private var miscPreferences: SharedPreferences
) {

  suspend fun initSettings() {
    if (!settingsRepository.isInitialized()) {
      settingsRepository.update(Settings.createInitial())
    }
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
      val suffix = if (DEBUG) "-debug" else ""
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

  suspend fun refreshTraktSyncSchedule(context: Context) {
    if (!settingsRepository.isInitialized()) return
    val schedule = settingsRepository.load().traktSyncSchedule
    TraktSyncWorker.schedule(schedule, context.applicationContext)
  }

  suspend fun refreshTraktQuickSync(context: Context) {
    if (quickSyncManager.isAnyScheduled()) {
      QuickSyncWorker.schedule(context)
    }
  }

  fun showWhatsNew(isInitialRun: Boolean): Boolean {
    val version = miscPreferences.getInt("APP_VERSION", 0)
    miscPreferences.edit().putInt("APP_VERSION", VERSION_CODE).apply()

    if (Config.SHOW_WHATS_NEW && VERSION_CODE > version && !isInitialRun) {
      return true
    }
    return false
  }

  fun isTutorialShown(tip: Tip) = when {
    DEBUG -> true
    else -> tipsRepository.isShown(tip)
  }

  fun setTutorialShown(tip: Tip) = tipsRepository.setShown(tip)

  fun clear() = ratingsRepository.clear()
}
