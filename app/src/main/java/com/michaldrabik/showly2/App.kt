package com.michaldrabik.showly2

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import com.michaldrabik.network.di.DaggerCloudComponent
import com.michaldrabik.showly2.di.AppComponent
import com.michaldrabik.showly2.di.DaggerAppComponent

class App : Application() {

  lateinit var appComponent: AppComponent

  override fun onCreate() {
    super.onCreate()
    createComponents()
  }

  private fun createComponents() {
    appComponent = DaggerAppComponent.builder()
      .cloudComponent(DaggerCloudComponent.create())
      .build()
  }
}

fun Activity.appComponent() = (application as App).appComponent

fun Fragment.appComponent() = (requireActivity().application as App).appComponent