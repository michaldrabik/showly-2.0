package com.michaldrabik.showly2

import android.app.Application
import android.app.NotificationChannel
import android.os.Build
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.jakewharton.processphoenix.ProcessPhoenix
import com.michaldrabik.common.Config
import com.michaldrabik.common.ConfigVariant
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.common.AppScopeProvider
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_base.utilities.extensions.notificationManager
import com.michaldrabik.ui_model.Settings
import com.michaldrabik.ui_widgets.calendar.CalendarWidgetProvider
import com.michaldrabik.ui_widgets.calendar_movies.CalendarMoviesWidgetProvider
import com.michaldrabik.ui_widgets.progress.ProgressWidgetProvider
import com.michaldrabik.ui_widgets.progress_movies.ProgressMoviesWidgetProvider
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject
import com.michaldrabik.ui_base.fcm.NotificationChannel as AppNotificationChannel

@HiltAndroidApp
class App :
  Application(),
  AppScopeProvider,
  Configuration.Provider,
  WidgetsProvider {

  override val appScope = MainScope()

  @Inject lateinit var workerFactory: HiltWorkerFactory
  @Inject lateinit var settingsRepository: SettingsRepository

  override fun onCreate() {

    fun setupSettings() = runBlocking {
      if (!settingsRepository.isInitialized()) {
        settingsRepository.update(Settings.createInitial())
      }
      FirebaseCrashlytics.getInstance().setUserId(settingsRepository.userId)
    }

    fun setupStrictMode() {
      if (BuildConfig.DEBUG) {
        StrictMode
          .setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
              .detectAll()
              .penaltyLog()
              .build()
          )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
              .detectUnsafeIntentLaunch()
              .penaltyDeath()
              .build()
          )
        }
      }
    }

    fun setupNotificationChannels() {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

      fun createChannel(channel: AppNotificationChannel) =
        NotificationChannel(channel.name, channel.displayName, channel.importance).apply {
          description = channel.description
        }

      notificationManager().run {
        createNotificationChannel(createChannel(AppNotificationChannel.GENERAL_INFO))
        createNotificationChannel(createChannel(AppNotificationChannel.SHOWS_INFO))
        createNotificationChannel(createChannel(AppNotificationChannel.EPISODES_ANNOUNCEMENTS))
        createNotificationChannel(createChannel(AppNotificationChannel.MOVIES_ANNOUNCEMENTS))
      }
    }

    fun setupTheme() {
      setDefaultNightMode(settingsRepository.theme)
    }

    fun setupRemoteConfig() {
      with(Firebase.remoteConfig) {
        setConfigSettingsAsync(
          remoteConfigSettings {
            minimumFetchIntervalInSeconds = ConfigVariant.REMOTE_CONFIG_FETCH_INTERVAL
          }
        )
        setDefaultsAsync(Config.REMOTE_CONFIG_DEFAULTS)
      }
    }

    super.onCreate()

    if (ProcessPhoenix.isPhoenixProcess(this)) return

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
      FirebaseMessaging.getInstance().token.addOnCompleteListener {
        val fcmToken = runCatching { it.result }.onFailure { Timber.w("Missing FCM Token") }
        Timber.d("FCM Token: $fcmToken")
      }
    }

    setupSettings()
    setupStrictMode()
    setupNotificationChannels()
    setupTheme()
    setupRemoteConfig()
  }

  override fun requestShowsWidgetsUpdate() {
    appScope.launch {
      ProgressWidgetProvider.requestUpdate(applicationContext)
      CalendarWidgetProvider.requestUpdate(applicationContext)
    }
  }

  override fun requestMoviesWidgetsUpdate() {
    appScope.launch {
      ProgressMoviesWidgetProvider.requestUpdate(applicationContext)
      CalendarMoviesWidgetProvider.requestUpdate(applicationContext)
    }
  }

  override fun getWorkManagerConfiguration() =
    Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .build()
}
