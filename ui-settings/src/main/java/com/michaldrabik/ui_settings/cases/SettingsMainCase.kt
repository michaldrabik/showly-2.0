package com.michaldrabik.ui_settings.cases

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.michaldrabik.common.Config
import com.michaldrabik.common.Mode
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_base.fcm.NotificationChannel
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_model.BuildConfig
import com.michaldrabik.ui_model.MyMoviesSection
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.MyShowsSection.FINISHED
import com.michaldrabik.ui_model.MyShowsSection.RECENTS
import com.michaldrabik.ui_model.MyShowsSection.UPCOMING
import com.michaldrabik.ui_model.MyShowsSection.WATCHING
import com.michaldrabik.ui_model.NotificationDelay
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_settings.helpers.AppLanguage
import javax.inject.Inject

@AppScope
class SettingsMainCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
  private val announcementManager: AnnouncementManager,
  private val showsImagesProvider: ShowImagesProvider,
  private val moviesImagesProvider: MovieImagesProvider
) {

  suspend fun getSettings(): Settings = settingsRepository.load()

  suspend fun setRecentShowsAmount(amount: Int) {
    check(amount in Config.MY_SHOWS_RECENTS_OPTIONS)
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(myRecentsAmount = amount)
      settingsRepository.update(new)
    }
  }

  @SuppressLint("NewApi")
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

  suspend fun enableAnnouncements(enable: Boolean, context: Context) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(episodesNotificationsEnabled = enable)
      settingsRepository.update(new)
      announcementManager.refreshShowsAnnouncements(context.applicationContext)
      announcementManager.refreshMoviesAnnouncements(context.applicationContext)
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

  suspend fun enableMyMoviesSection(section: MyMoviesSection, isEnabled: Boolean) {
    val settings = settingsRepository.load()
    settings.let {
      val new = when (section) {
        MyMoviesSection.RECENTS -> it.copy(myMoviesRecentIsEnabled = isEnabled)
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

  suspend fun enableSpecialSeasons(enable: Boolean) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(specialSeasonsEnabled = enable)
      settingsRepository.update(new)
    }
  }

  fun isMoviesEnabled() = settingsRepository.isMoviesEnabled

  suspend fun enableMovies(enable: Boolean, context: Context) {
    val newMode = if (!enable) Mode.SHOWS else settingsRepository.mode
    settingsRepository.run {
      isMoviesEnabled = enable
      mode = newMode
    }
    announcementManager.refreshMoviesAnnouncements(context.applicationContext)
  }

  suspend fun enableWidgetsTitles(enable: Boolean, context: Context) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(widgetsShowLabel = enable)
      settingsRepository.update(new)
    }
    (context.applicationContext as WidgetsProvider).run {
      requestShowsWidgetsUpdate()
      requestMoviesWidgetsUpdate()
    }
  }

  suspend fun setWhenToNotify(delay: NotificationDelay, context: Context) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(episodesNotificationsDelay = delay)
      settingsRepository.update(new)
      announcementManager.refreshShowsAnnouncements(context.applicationContext)
    }
  }

  fun getLanguage() = AppLanguage.fromCode(settingsRepository.language)

  suspend fun setLanguage(language: AppLanguage) {
    settingsRepository.run {
      this.language = language.code
      val unused = AppLanguage.values()
        .filter { it.code != Config.DEFAULT_LANGUAGE && it != language }
        .map { it.code }
      clearUnusedTranslations(unused)
      clearLanguageLogs()
    }
  }

  fun getCountry() = AppCountry.fromCode(settingsRepository.country)

  fun setCountry(country: AppCountry) {
    settingsRepository.country = country.code
  }

  fun isPremium() = settingsRepository.isPremium

  suspend fun deleteImagesCache() {
    showsImagesProvider.deleteLocalCache()
    moviesImagesProvider.deleteLocalCache()
  }
}
