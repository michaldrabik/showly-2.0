package com.michaldrabik.ui_base.events

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class EventsFragmentCallbacks : FragmentManager.FragmentLifecycleCallbacks() {
  override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
    super.onFragmentViewCreated(fm, f, v, savedInstanceState)
    if (f is EventObserver) EventsManager.registerObserver(f)
  }

  override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
    if (f is EventObserver) EventsManager.removeObserver(f)
    super.onFragmentViewDestroyed(fm, f)
  }
}
