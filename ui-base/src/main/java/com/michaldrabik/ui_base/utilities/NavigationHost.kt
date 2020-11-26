package com.michaldrabik.ui_base.utilities

import androidx.annotation.IdRes

interface NavigationHost {
  fun setMode(mode: Mode)

  fun hideNavigation(animate: Boolean)
  fun showNavigation(animate: Boolean)

  fun openTab(@IdRes navigationId: Int)
  fun openDiscoverTab()
}
