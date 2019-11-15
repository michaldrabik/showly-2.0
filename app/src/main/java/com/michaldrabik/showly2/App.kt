package com.michaldrabik.showly2

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.os.Build
import android.os.StrictMode
import androidx.fragment.app.Fragment
import com.jakewharton.threetenabp.AndroidThreeTen
import com.michaldrabik.network.di.DaggerCloudComponent
import com.michaldrabik.showly2.di.AppComponent
import com.michaldrabik.showly2.di.DaggerAppComponent
import com.michaldrabik.showly2.fcm.FcmChannel
import com.michaldrabik.showly2.fcm.FcmChannel.GENERAL_INFO
import com.michaldrabik.showly2.fcm.FcmChannel.SHOWS_INFO
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

    fun createChannel(channel: FcmChannel) =
      NotificationChannel(channel.name, channel.displayName, channel.importance).apply {
        description = channel.description
      }

    (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).run {
      createNotificationChannel(createChannel(GENERAL_INFO))
      createNotificationChannel(createChannel(SHOWS_INFO))
    }
  }
}

fun Activity.appComponent() = (application as App).appComponent

fun Service.appComponent() = (application as App).appComponent

fun Fragment.appComponent() = (requireActivity().application as App).appComponent
