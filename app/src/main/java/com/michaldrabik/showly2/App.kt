package com.michaldrabik.showly2

import android.app.Application
import android.app.NotificationChannel
import android.os.Build
import android.os.StrictMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.jakewharton.processphoenix.ProcessPhoenix
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

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .build()

  override fun onCreate() {

    fun setupSettings() = runBlocking {
      if (!settingsRepository.isInitialized()) {
        settingsRepository.update(Settings.createInitial())
      }
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
        NotificationChannel(
          /* id = */ channel.name,
          /* name = */ channel.displayName,
          /* importance = */ channel.importance
        ).apply {
          description = channel.description
        }

      notificationManager().run {
        createNotificationChannel(createChannel(AppNotificationChannel.GENERAL_INFO))
        createNotificationChannel(createChannel(AppNotificationChannel.SHOWS_INFO))
        createNotificationChannel(createChannel(AppNotificationChannel.EPISODES_ANNOUNCEMENTS))
        createNotificationChannel(createChannel(AppNotificationChannel.MOVIES_ANNOUNCEMENTS))
      }
    }

    super.onCreate()

    if (ProcessPhoenix.isPhoenixProcess(this)) return

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }

    setupSettings()
    setupStrictMode()
    setupNotificationChannels()
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
}
