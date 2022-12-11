package com.michaldrabik.ui_settings.cases

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.messaging.FirebaseMessaging
import com.michaldrabik.common.Config
import com.michaldrabik.common.ConfigVariant
import com.michaldrabik.common.Mode
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_base.dates.AppDateFormat
import com.michaldrabik.ui_base.fcm.NotificationChannel
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_model.MyMoviesSection
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.MyShowsSection.RECENTS
import com.michaldrabik.ui_model.NotificationDelay
import com.michaldrabik.ui_model.ProgressNextEpisodeType
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_settings.helpers.AppLanguage
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SettingsMainCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
  private val announcementManager: AnnouncementManager,
  private val showsImagesProvider: ShowImagesProvider,
  private val moviesImagesProvider: MovieImagesProvider,
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

  suspend fun enableMyShowsSection(section: MyShowsSection, isEnabled: Boolean) {
    val settings = settingsRepository.load()
    settings.let {
      val new = when (section) {
        RECENTS -> it.copy(myShowsRecentIsEnabled = isEnabled)
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

  suspend fun enableSpecialSeasons(enable: Boolean) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(specialSeasonsEnabled = enable)
      settingsRepository.update(new)
    }
  }

  suspend fun enableProgressUpcoming(enable: Boolean, context: Context) {
    with(settingsRepository) {
      val updatedSettings = load().copy(progressUpcomingEnabled = enable)
      update(updatedSettings)
    }
    (context.applicationContext as WidgetsProvider).run {
      requestShowsWidgetsUpdate()
    }
  }

  fun isMoviesEnabled() = settingsRepository.isMoviesEnabled

  suspend fun enableMovies(enable: Boolean) {
    val newMode = if (!enable) Mode.SHOWS else settingsRepository.mode
    settingsRepository.run {
      isMoviesEnabled = enable
      mode = newMode
    }
    announcementManager.refreshMoviesAnnouncements()
  }

  fun isNewsEnabled() = settingsRepository.isNewsEnabled

  fun enableNews(enable: Boolean) {
    settingsRepository.run {
      isNewsEnabled = enable
    }
  }

  fun isStreamingsEnabled() = settingsRepository.streamingsEnabled

  fun enableStreamings(enable: Boolean) {
    settingsRepository.run {
      streamingsEnabled = enable
    }
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

  suspend fun setWhenToNotify(delay: NotificationDelay) {
    val settings = settingsRepository.load()
    settings.let {
      val new = it.copy(episodesNotificationsDelay = delay)
      settingsRepository.update(new)
      announcementManager.refreshShowsAnnouncements()
    }
  }

  suspend fun getLanguage(): AppLanguage {
    if (Build.VERSION.SDK_INT >= TIRAMISU) {
      val locales = AppCompatDelegate.getApplicationLocales()
      if (!locales.isEmpty) {
        val locale = locales.get(0)!!.language
        val language = AppLanguage.fromCode(locale)
        if (settingsRepository.language != locale) {
          setLanguage(language)
        }
        return language
      }
    }
    return AppLanguage.fromCode(settingsRepository.language)
  }

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

  fun getProgressType() = settingsRepository.progressNextEpisodeType

  fun setProgressType(type: ProgressNextEpisodeType) {
    settingsRepository.progressNextEpisodeType = type
  }

  fun isPremium() = settingsRepository.isPremium

  fun setDateFormat(format: AppDateFormat, context: Context) {
    settingsRepository.dateFormat = format.name
    (context.applicationContext as WidgetsProvider).run {
      requestShowsWidgetsUpdate()
      requestMoviesWidgetsUpdate()
    }
  }

  fun getDateFormat() = AppDateFormat.valueOf(settingsRepository.dateFormat)

  fun getUserId() = settingsRepository.userId

  suspend fun deleteImagesCache() {
    showsImagesProvider.deleteLocalCache()
    moviesImagesProvider.deleteLocalCache()
  }
}
