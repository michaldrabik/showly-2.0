package com.michaldrabik.showly2.ui

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fcm.FcmExtra
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.common.OnTraktAuthorizeListener
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_widgets.BaseWidgetProvider.Companion.EXTRA_MOVIE_ID
import com.michaldrabik.ui_widgets.BaseWidgetProvider.Companion.EXTRA_SHOW_ID
import com.michaldrabik.ui_widgets.search.SearchWidgetProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_bottom_menu.*

abstract class BaseActivity : AppCompatActivity() {

  private val showActionKeys = arrayOf(
    FcmExtra.SHOW_ID.key,
    EXTRA_SHOW_ID,
    EXTRA_MOVIE_ID
  )

  protected fun findNavHostFragment() = supportFragmentManager.findFragmentById(R.id.navigationHost) as? NavHostFragment

  fun handleNotification(extras: Bundle?, action: () -> Unit = {}) {
    if (extras == null) return
    if (extras.containsKey(SearchWidgetProvider.EXTRA_WIDGET_SEARCH_CLICK)) {
      handleSearchWidgetClick(extras)
      return
    }
    showActionKeys.forEach {
      if (extras.containsKey(it)) {
        handleFcmShowPush(extras, it, action)
      }
    }
  }

  private fun handleSearchWidgetClick(extras: Bundle?) {
    findNavHostFragment()?.findNavController()?.run {
      try {
        when (currentDestination?.id) {
          R.id.searchFragment -> return@run
          R.id.showDetailsFragment, R.id.movieDetailsFragment -> navigateUp()
        }
        if (currentDestination?.id != R.id.discoverFragment) {
          bottomNavigationView.selectedItemId = R.id.menuDiscover
        }
        when (currentDestination?.id) {
          R.id.discoverFragment -> navigate(R.id.actionDiscoverFragmentToSearchFragment)
          R.id.discoverMoviesFragment -> navigate(R.id.actionDiscoverMoviesFragmentToSearchFragment)
        }
        extras?.clear()
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "BaseActivity::handleSearchWidgetClick()")
      }
    }
  }

  private fun handleFcmShowPush(extras: Bundle, key: String, action: () -> Unit) {
    val itemId = extras.getString(key)?.toLong() ?: -1
    val isShow = key in arrayOf(EXTRA_SHOW_ID, FcmExtra.SHOW_ID.key)

    val bundle = Bundle().apply {
      putLong(ARG_SHOW_ID, itemId)
      putLong(ARG_MOVIE_ID, itemId)
    }

    findNavHostFragment()?.findNavController()?.run {
      try {
        if (currentDestination?.id in arrayOf(
            R.id.showDetailsFragment,
            R.id.movieDetailsFragment,
            R.id.settingsFragment,
            R.id.traktSyncFragment
          )
        ) {
          popBackStack()
        }
        when (currentDestination?.id) {
          R.id.showDetailsFragment -> navigate(R.id.actionShowDetailsFragmentToSelf, bundle)
          R.id.movieDetailsFragment -> navigate(R.id.actionMovieDetailsFragmentToSelf, bundle)
          else -> {
            bottomNavigationView.selectedItemId = R.id.menuProgress
            val actionId = when (currentDestination?.id) {
              R.id.progressFragment -> {
                if (isShow) R.id.actionProgressFragmentToShowDetailsFragment
                else R.id.actionProgressFragmentToMovieDetailsFragment
              }
              R.id.progressMoviesMainFragment -> {
                if (isShow) R.id.actionProgressMoviesFragmentToShowDetailsFragment
                else R.id.actionProgressMoviesFragmentToMovieDetailsFragment
              }
              else -> {
                error("Unknown actionId. Key $key, actionId: ${currentDestination?.id}")
              }
            }
            navigate(actionId, bundle)
          }
        }
        extras.clear()
        action()
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "BaseActivity::handleFcmShowPush()")
      }
    }
  }

  protected fun handleTraktAuthorization(authData: Uri?) {
    findNavHostFragment()?.findNavController()?.currentDestination?.id?.let {
      val navHost = supportFragmentManager.findFragmentById(R.id.navigationHost)
      navHost?.childFragmentManager?.primaryNavigationFragment?.let {
        (it as? OnTraktAuthorizeListener)?.onAuthorizationResult(authData)
      }
    }
  }
}
