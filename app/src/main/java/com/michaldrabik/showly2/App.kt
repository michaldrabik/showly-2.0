package com.michaldrabik.showly2

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.Service
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.StrictMode
import com.google.firebase.messaging.FirebaseMessaging
import com.jakewharton.threetenabp.AndroidThreeTen
import com.michaldrabik.network.di.DaggerCloudComponent
import com.michaldrabik.showly2.common.ShowsSyncActivityCallbacks
import com.michaldrabik.showly2.di.component.AppComponent
import com.michaldrabik.showly2.di.component.DaggerAppComponent
import com.michaldrabik.showly2.di.module.PreferencesModule
import com.michaldrabik.showly2.utilities.NetworkMonitorCallbacks
import com.michaldrabik.storage.di.DaggerStorageComponent
import com.michaldrabik.storage.di.StorageModule
import com.michaldrabik.ui_base.common.OnlineStatusProvider
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_base.di.UiBaseComponentProvider
import com.michaldrabik.ui_base.events.EventsActivityCallbacks
import com.michaldrabik.ui_base.utilities.extensions.notificationManager
import com.michaldrabik.ui_widgets.di.UiWidgetsComponentProvider
import com.michaldrabik.ui_widgets.watchlist.WatchlistWidgetProvider
import timber.log.Timber
import com.michaldrabik.ui_base.fcm.NotificationChannel as AppNotificationChannel

class App :
  Application(),
  UiBaseComponentProvider,
  UiWidgetsComponentProvider,
  OnlineStatusProvider,
  WidgetsProvider {

  lateinit var appComponent: AppComponent
  var isAppOnline = true

  private val activityCallbacks by lazy {
    listOf(
      EventsActivityCallbacks(),
      ShowsSyncActivityCallbacks(),
      NetworkMonitorCallbacks(connectivityManager())
    )
  }

  override fun onCreate() {
    super.onCreate()
    activityCallbacks.forEach { registerActivityLifecycleCallbacks(it) }

    AndroidThreeTen.init(this)
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
      FirebaseMessaging.getInstance().token.addOnCompleteListener {
        Timber.d("FCM Token: ${it.result}")
      }
    }

    setupComponents()
    setupStrictMode()
    setupNotificationChannels()
  }

  private fun setupComponents() {
    appComponent = DaggerAppComponent.builder()
      .cloudMarker(DaggerCloudComponent.create())
      .storageMarker(
        DaggerStorageComponent.builder()
          .storageModule(StorageModule(applicationContext))
          .build()
      )
      .preferencesModule(PreferencesModule(applicationContext))
      .build()
  }

  private fun setupStrictMode() {
    if (BuildConfig.DEBUG) {
      StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
          .detectAll()
          .penaltyLog()
          .build()
      )
    }
  }

  private fun setupNotificationChannels() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    fun createChannel(channel: AppNotificationChannel) =
      NotificationChannel(channel.name, channel.displayName, channel.importance).apply {
        description = channel.description
      }

    notificationManager().run {
      createNotificationChannel(createChannel(AppNotificationChannel.GENERAL_INFO))
      createNotificationChannel(createChannel(AppNotificationChannel.SHOWS_INFO))
      createNotificationChannel(createChannel(AppNotificationChannel.EPISODES_ANNOUNCEMENTS))
    }
  }

  override fun isOnline() = isAppOnline
  override fun requestWidgetsUpdate() = WatchlistWidgetProvider.requestUpdate(applicationContext)

  override fun provideBaseComponent() = appComponent.uiBaseComponent().create()
  override fun provideWidgetsComponent() = appComponent.uiWidgetsComponent().create()
}

fun Context.connectivityManager() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

fun Activity.appComponent() = (application as App).appComponent

fun Service.serviceComponent() = (application as App).appComponent.serviceComponent().create()
