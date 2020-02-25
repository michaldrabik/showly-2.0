package com.michaldrabik.showly2.common.events

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class EventsActivityCallbacks : Application.ActivityLifecycleCallbacks {

  private val eventsFragmentCallbacks by lazy { EventsFragmentCallbacks() }

  override fun onActivityCreated(activity: Activity, p1: Bundle?) {
    (activity as AppCompatActivity).supportFragmentManager
      .registerFragmentLifecycleCallbacks(eventsFragmentCallbacks, true)
  }

  override fun onActivityStarted(activity: Activity) =
    EventsManager.registerObserver(activity as EventObserver)

  override fun onActivityStopped(activity: Activity) =
    EventsManager.removeObserver(activity as EventObserver)

  override fun onActivityDestroyed(activity: Activity) {
    (activity as AppCompatActivity).supportFragmentManager
      .unregisterFragmentLifecycleCallbacks(eventsFragmentCallbacks)
  }

  override fun onActivityPaused(activity: Activity) = Unit

  override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) = Unit

  override fun onActivityResumed(activity: Activity) = Unit
}
