package com.michaldrabik.showly2.ui.main.cases

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessaging
import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.showly2.BuildConfig
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.fcm.NotificationChannel
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_repository.RatingsRepository
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.UserTraktManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject
import javax.inject.Named

@AppScope
class MainInitialsCase @Inject constructor(
  private val userTraktManager: UserTraktManager,
  private val ratingsRepository: RatingsRepository,
  private val settingsRepository: SettingsRepository,
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

  @SuppressLint("NewApi")
  suspend fun initFcm() {
    FirebaseMessaging.getInstance().run {
      val isEnabled = settingsRepository.load().pushNotificationsEnabled
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

  suspend fun initRatings() = supervisorScope {
    try {
      if (!userTraktManager.isAuthorized()) return@supervisorScope
      val token = userTraktManager.checkAuthorization().token

      awaitAll(
        async { ratingsRepository.shows.preloadShowsRatings(token) },
        async { ratingsRepository.shows.preloadEpisodesRatings(token) },
        async {
          if (settingsRepository.isMoviesEnabled()) {
            ratingsRepository.movies.preloadMoviesRatings(token)
          }
        }
      )
    } catch (error: Throwable) {
      Logger.record(error, "Source" to "${MainInitialsCase::class.simpleName}::initRatings()")
    }
  }

  fun showWhatsNew(isInitialRun: Boolean): Boolean {
    val keyAppVersion = "APP_VERSION"
    val keyAppVersionName = "APP_VERSION_NAME"

    val version = miscPreferences.getInt(keyAppVersion, 0)
    val name = miscPreferences.getString(keyAppVersionName, "")

    fun isPatchUpdate(): Boolean {
      if (name.isNullOrBlank()) return false

      val major = name.split(".").getOrNull(0)?.toIntOrNull()
      val minor = name.split(".").getOrNull(1)?.toIntOrNull()

      val currentMajor = BuildConfig.VERSION_NAME.split(".").getOrNull(0)?.toIntOrNull()
      val currentMinor = BuildConfig.VERSION_NAME.split(".").getOrNull(1)?.toIntOrNull()

      if (major == currentMajor && minor == currentMinor) return true
      return false
    }

    miscPreferences.edit {
      putInt(keyAppVersion, BuildConfig.VERSION_CODE).apply()
      putString(keyAppVersionName, BuildConfig.VERSION_NAME).apply()
    }

    if (Config.SHOW_WHATS_NEW &&
      BuildConfig.VERSION_CODE > version &&
      BuildConfig.VERSION_NAME != name &&
      !isInitialRun &&
      !isPatchUpdate()
    ) {
      return true
    }
    return false
  }
}
