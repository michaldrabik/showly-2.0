package com.michaldrabik.showly2.ui

import android.net.Uri
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fcm.FcmExtra
import com.michaldrabik.showly2.ui.common.base.BaseActivity
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment
import com.michaldrabik.showly2.widget.search.SearchWidgetProvider
import com.michaldrabik.showly2.widget.watchlist.WatchlistWidgetProvider
import com.michaldrabik.ui_base.common.OnTraktAuthorizeListener
import kotlinx.android.synthetic.main.activity_main.*

abstract class NotificationActivity : BaseActivity() {

  private val showActionKeys = arrayOf(
    FcmExtra.SHOW_ID.key,
    WatchlistWidgetProvider.EXTRA_SHOW_ID
  )

  protected fun handleNotification(extras: Bundle?, action: () -> Unit = {}) {
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
    navigationHost.findNavController().run {
      try {
        if (currentDestination?.id == R.id.showDetailsFragment) {
          navigateUp()
        }
        if (currentDestination?.id != R.id.discoverFragment) {
          bottomNavigationView.selectedItemId = R.id.menuDiscover
        }
        navigate(R.id.actionDiscoverFragmentToSearchFragment)
        extras?.clear()
      } catch (e: Throwable) {
        val exception = Throwable(NotificationActivity::class.simpleName, e)
        FirebaseCrashlytics.getInstance().recordException(exception)
      }
    }
  }

  private fun handleFcmShowPush(extras: Bundle, key: String, action: () -> Unit) {
    val showId = extras.getString(key)?.toLong() ?: -1
    val bundle = Bundle().apply { putLong(ShowDetailsFragment.ARG_SHOW_ID, showId) }
    navigationHost.findNavController().run {
      try {
        when (currentDestination?.id) {
          R.id.showDetailsFragment -> navigate(R.id.actionShowDetailsFragmentToSelf, bundle)
          else -> {
            bottomNavigationView.selectedItemId = R.id.menuWatchlist
            navigate(R.id.actionWatchlistFragmentToShowDetailsFragment, bundle)
          }
        }
        extras.clear()
        action()
      } catch (e: Exception) {
        val exception = Throwable(NotificationActivity::class.simpleName, e)
        FirebaseCrashlytics.getInstance().recordException(exception)
      }
    }
  }

  protected fun handleTraktAuthorization(authData: Uri?) {
    navigationHost.findNavController().currentDestination?.id?.let {
      val navHost = supportFragmentManager.findFragmentById(R.id.navigationHost)
      navHost?.childFragmentManager?.primaryNavigationFragment?.let {
        (it as? OnTraktAuthorizeListener)?.onAuthorizationResult(authData)
      }
    }
  }
}
