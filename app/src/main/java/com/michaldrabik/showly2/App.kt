package com.michaldrabik.showly2

import android.app.Activity
import android.app.Application
import android.os.StrictMode
import androidx.fragment.app.Fragment
import com.jakewharton.threetenabp.AndroidThreeTen
import com.michaldrabik.network.di.DaggerCloudComponent
import com.michaldrabik.showly2.di.AppComponent
import com.michaldrabik.showly2.di.DaggerAppComponent
import com.michaldrabik.storage.di.DaggerStorageComponent
import com.michaldrabik.storage.di.StorageModule

class App : Application() {

  lateinit var appComponent: AppComponent

  override fun onCreate() {
    super.onCreate()
    createComponents()
    AndroidThreeTen.init(this)
    setStrictMode()
  }

  private fun createComponents() {
    appComponent = DaggerAppComponent.builder()
      .cloudMarker(DaggerCloudComponent.create())
      .storageMarker(
        DaggerStorageComponent.builder()
          .storageModule(StorageModule(this))
          .build()
      )
      .build()
  }

  private fun setStrictMode() {
    if (BuildConfig.DEBUG) {
      StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
          .detectAll()
          .penaltyLog()
          .build()
      )
    }
  }
}

fun Activity.appComponent() = (application as App).appComponent

fun Fragment.appComponent() = (requireActivity().application as App).appComponent