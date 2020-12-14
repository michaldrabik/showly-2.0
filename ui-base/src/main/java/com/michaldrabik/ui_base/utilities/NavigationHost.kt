package com.michaldrabik.ui_base.utilities

import androidx.annotation.IdRes
import androidx.navigation.fragment.NavHostFragment
import com.michaldrabik.common.Mode

interface NavigationHost {
  fun moviesEnabled(): Boolean
  fun setMode(mode: Mode)
  fun getMode(): Mode

  fun findNavHost(): NavHostFragment
  fun hideNavigation(animate: Boolean)
  fun showNavigation(animate: Boolean)

  fun openTab(@IdRes navigationId: Int)
  fun openDiscoverTab()
}
