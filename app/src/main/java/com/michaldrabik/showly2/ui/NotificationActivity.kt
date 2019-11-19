package com.michaldrabik.showly2.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fcm.FcmExtra
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment
import kotlinx.android.synthetic.main.activity_main.*

abstract class NotificationActivity : AppCompatActivity() {

  protected fun handleNotification(extras: Bundle?, action: () -> Unit = {}) {
    if (extras == null) return

    if (extras.containsKey(FcmExtra.SHOW_ID.key)) {
      handleFcmShowPush(extras, action)
    }
  }

  private fun handleFcmShowPush(extras: Bundle, action: () -> Unit) {
    val showId = extras.getString(FcmExtra.SHOW_ID.key)?.toLong() ?: -1
    val bundle = Bundle().apply { putLong(ShowDetailsFragment.ARG_SHOW_ID, showId) }
    navigationHost.findNavController().run {
      try {
        val current = currentDestination?.id
        when (current) {
          R.id.showDetailsFragment -> navigate(R.id.actionShowDetailsFragmentToSelf, bundle)
          else -> {
            bottomNavigationView.selectedItemId = R.id.menuWatchlist
            navigate(R.id.actionWatchlistFragmentToShowDetailsFragment, bundle)
          }
        }
        extras.clear()
        action()
      } catch (e: Exception) {
        //NOOP Simply leave app where it is in case of any failure
      }
    }
  }
}
