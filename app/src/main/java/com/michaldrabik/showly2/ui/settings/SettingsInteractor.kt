package com.michaldrabik.showly2.ui.settings

import android.content.Context
import android.net.Uri
import com.google.firebase.messaging.FirebaseMessaging
import com.michaldrabik.showly2.BuildConfig
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.common.images.ShowImagesProvider
import com.michaldrabik.showly2.common.notifications.AnnouncementManager
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.fcm.NotificationChannel
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.MyShowsSection.COMING_SOON
import com.michaldrabik.showly2.model.MyShowsSection.ENDED
import com.michaldrabik.showly2.model.MyShowsSection.RECENTS
import com.michaldrabik.showly2.model.MyShowsSection.RUNNING
import com.michaldrabik.showly2.model.NotificationDelay
import com.michaldrabik.showly2.model.Settings
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.repository.rating.RatingsRepository
import com.michaldrabik.showly2.repository.settings.SettingsRepository
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class SettingsInteractor @Inject constructor(
  private val settingsRepository: SettingsRepository,
  private val showsRepository: ShowsRepository,
  private val ratingsRepository: RatingsRepository,
  private val announcementManager: AnnouncementManager,
  private val userManager: UserTraktManager,
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
        RUNNING -> it.copy(myShowsRunningIsEnabled = isEnabled)
        ENDED -> it.copy(myShowsEndedIsEnabled = isEnabled)
        COMING_SOON -> it.copy(myShowsIncomingIsEnabled = isEnabled)
        else -> error("Should not be used here.")
      }
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

  suspend fun authorizeTrakt(authData: Uri) {
    val code = authData.getQueryParameter("code")
    if (code.isNullOrBlank()) {
      throw IllegalStateException("Invalid Trakt authorization code.")
    }
    userManager.authorize(code)
  }

  suspend fun logoutTrakt() {
    userManager.revokeToken()
    ratingsRepository.clear()
  }

  suspend fun deleteImagesCache() = imagesProvider.deleteLocalCache()

  suspend fun isTraktAuthorized() = userManager.isAuthorized()

  fun getTraktUsername() = userManager.traktUsername
}
