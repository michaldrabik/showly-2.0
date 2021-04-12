package com.michaldrabik.ui_base.utilities

import androidx.annotation.IdRes
import androidx.navigation.NavController
import com.michaldrabik.common.Mode

interface NavigationHost {
  fun moviesEnabled(): Boolean
  fun setMode(mode: Mode, force: Boolean = false)
  fun getMode(): Mode

  fun findNavControl(): NavController?
  fun hideNavigation(animate: Boolean)
  fun showNavigation(animate: Boolean)

  fun openTab(@IdRes navigationId: Int)
  fun openDiscoverTab()
}
