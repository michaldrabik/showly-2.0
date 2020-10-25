package com.michaldrabik.showly2.common

import android.app.Activity
import android.app.Application
import android.os.Bundle

class ShowsSyncActivityCallbacks : Application.ActivityLifecycleCallbacks {

  override fun onActivityStarted(activity: Activity) {
    ShowsSyncService.initialize(activity.applicationContext)
    TranslationsSyncService.initialize(activity.applicationContext)
  }

  override fun onActivityCreated(activity: Activity, p1: Bundle?) = Unit
  override fun onActivityStopped(activity: Activity) = Unit
  override fun onActivityDestroyed(activity: Activity) = Unit
  override fun onActivityPaused(activity: Activity) = Unit
  override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) = Unit
  override fun onActivityResumed(activity: Activity) = Unit
}
