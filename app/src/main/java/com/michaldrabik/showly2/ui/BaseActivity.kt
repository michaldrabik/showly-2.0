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
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_widgets.progress.ProgressWidgetProvider
import com.michaldrabik.ui_widgets.search.SearchWidgetProvider
import kotlinx.android.synthetic.main.activity_main.*

abstract class BaseActivity : AppCompatActivity() {

  private val showActionKeys = arrayOf(
    FcmExtra.SHOW_ID.key,
    ProgressWidgetProvider.EXTRA_SHOW_ID
  )

  fun findNavHostFragment() = supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment

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
    findNavHostFragment().findNavController().run {
      try {
        when (currentDestination?.id) {
          R.id.searchFragment -> return@run
          R.id.showDetailsFragment -> navigateUp()
        }
        if (currentDestination?.id != R.id.discoverFragment) {
          bottomNavigationView.selectedItemId = R.id.menuDiscover
        }
        navigate(R.id.actionDiscoverFragmentToSearchFragment)
        extras?.clear()
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "${BaseActivity::class.simpleName}::handleSearchWidgetClick()")
      }
    }
  }

  private fun handleFcmShowPush(extras: Bundle, key: String, action: () -> Unit) {
    val showId = extras.getString(key)?.toLong() ?: -1
    val bundle = Bundle().apply { putLong(ARG_SHOW_ID, showId) }
    findNavHostFragment().findNavController().run {
      try {
        when (currentDestination?.id) {
          R.id.showDetailsFragment -> navigate(R.id.actionShowDetailsFragmentToSelf, bundle)
          else -> {
            bottomNavigationView.selectedItemId = R.id.menuProgress
            navigate(R.id.actionProgressFragmentToShowDetailsFragment, bundle)
          }
        }
        extras.clear()
        action()
      } catch (e: Exception) {
        Logger.record(e, "Source" to "${BaseActivity::class.simpleName}::handleFcmShowPush()")
      }
    }
  }

  protected fun handleTraktAuthorization(authData: Uri?) {
    findNavHostFragment().findNavController().currentDestination?.id?.let {
      val navHost = supportFragmentManager.findFragmentById(R.id.navigationHost)
      navHost?.childFragmentManager?.primaryNavigationFragment?.let {
        (it as? OnTraktAuthorizeListener)?.onAuthorizationResult(authData)
      }
    }
  }
}
