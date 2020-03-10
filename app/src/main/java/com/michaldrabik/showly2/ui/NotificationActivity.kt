package com.michaldrabik.showly2.ui

import android.net.Uri
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.crashlytics.android.Crashlytics
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fcm.FcmExtra
import com.michaldrabik.showly2.ui.common.OnTraktAuthorizeListener
import com.michaldrabik.showly2.ui.main.BaseActivity
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment
import com.michaldrabik.showly2.widget.WatchlistAppWidgetProvider
import kotlinx.android.synthetic.main.activity_main.*

abstract class NotificationActivity : BaseActivity() {

  private val keys = arrayOf(
    FcmExtra.SHOW_ID.key,
    WatchlistAppWidgetProvider.EXTRA_SHOW_ID
  )

  protected fun handleNotification(extras: Bundle?, action: () -> Unit = {}) {
    if (extras == null) return
    keys.forEach {
      if (extras.containsKey(it)) {
        handleFcmShowPush(extras, it, action)
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
        // NOOP Simply leave app where it is in case of failure
        Crashlytics.logException(e)
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
