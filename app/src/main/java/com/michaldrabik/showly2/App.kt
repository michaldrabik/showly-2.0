package com.michaldrabik.showly2

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.Service
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.StrictMode
import androidx.fragment.app.Fragment
import com.jakewharton.threetenabp.AndroidThreeTen
import com.michaldrabik.network.di.DaggerCloudComponent
import com.michaldrabik.showly2.di.AppComponent
import com.michaldrabik.showly2.di.DaggerAppComponent
import com.michaldrabik.showly2.fcm.NotificationChannel.EPISODES_ANNOUNCEMENTS
import com.michaldrabik.showly2.fcm.NotificationChannel.GENERAL_INFO
import com.michaldrabik.showly2.fcm.NotificationChannel.SHOWS_INFO
import com.michaldrabik.showly2.utilities.extensions.notificationManager
import com.michaldrabik.storage.di.DaggerStorageComponent
import com.michaldrabik.storage.di.StorageModule

class App : Application() {

  lateinit var appComponent: AppComponent

  override fun onCreate() {
    super.onCreate()
    AndroidThreeTen.init(this)
    setupComponents()
    setupStrictMode()
    setupNotificationChannels()
  }

  private fun setupComponents() {
    appComponent = DaggerAppComponent.builder()
      .cloudMarker(DaggerCloudComponent.create())
      .storageMarker(
        DaggerStorageComponent.builder()
          .storageModule(StorageModule(this))
          .build()
      )
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

    fun createChannel(channel: com.michaldrabik.showly2.fcm.NotificationChannel) =
      NotificationChannel(channel.name, channel.displayName, channel.importance).apply {
        description = channel.description
      }

    notificationManager().run {
      createNotificationChannel(createChannel(GENERAL_INFO))
      createNotificationChannel(createChannel(SHOWS_INFO))
      createNotificationChannel(createChannel(EPISODES_ANNOUNCEMENTS))
    }
  }
}

fun Context.connectivityManager() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

fun Activity.appComponent() = (application as App).appComponent

fun Service.appComponent() = (application as App).appComponent

fun Fragment.appComponent() = (requireActivity().application as App).appComponent
