package com.michaldrabik.ui_base

import androidx.annotation.IdRes

interface NavigationHost {
  fun hideNavigation(animate: Boolean)
  fun showNavigation(animate: Boolean)

  fun openTab(@IdRes navigationId: Int)
}
