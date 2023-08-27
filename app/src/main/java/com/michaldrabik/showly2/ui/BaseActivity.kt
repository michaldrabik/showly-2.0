package com.michaldrabik.showly2.ui

import android.annotation.SuppressLint
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

abstract class BaseActivity : AppCompatActivity() {

  private val actionKeys = arrayOf(
    FcmExtra.SHOW_ID.key,
    EXTRA_SHOW_ID,
    EXTRA_MOVIE_ID
  )

  protected fun findNavHostFragment() = supportFragmentManager.findFragmentById(R.id.navigationHost) as? NavHostFragment

  protected abstract fun handleSearchWidgetClick(bundle: Bundle?)

  fun handleNotification(extras: Bundle?, action: () -> Unit = {}) {
    if (extras == null) return
    if (extras.containsKey(SearchWidgetProvider.EXTRA_WIDGET_SEARCH_CLICK)) {
      handleSearchWidgetClick(extras)
      return
    }
    actionKeys.forEach {
      if (extras.containsKey(it)) {
        handleShowMovieExtra(extras, it, action)
      }
    }
  }

  @SuppressLint("RestrictedApi")
  private fun handleShowMovieExtra(extras: Bundle, key: String, action: () -> Unit) {
    val itemId = extras.getString(key)?.toLong() ?: -1
    val bundle = Bundle().apply {
      putLong(ARG_SHOW_ID, itemId)
      putLong(ARG_MOVIE_ID, itemId)
    }

    findNavHostFragment()?.findNavController()?.run {
      try {
        val isShow = key in arrayOf(EXTRA_SHOW_ID, FcmExtra.SHOW_ID.key)
        if (isShow) {
          navigate(R.id.actionNavigateShowDetailsFragment, bundle)
        } else {
          navigate(R.id.actionNavigateMovieDetailsFragment, bundle)
        }
        extras.clear()
        action()
      } catch (error: Throwable) {
        Logger.record(error, "BaseActivity::handleShowMovieExtra()")
      }
    }
  }

  protected fun handleTraktAuthorization(authData: Uri?) {
    findNavHostFragment()?.findNavController()?.currentDestination?.id?.let {
      val navHost = supportFragmentManager.findFragmentById(R.id.navigationHost)
      navHost?.childFragmentManager?.primaryNavigationFragment?.let {
        if (authData.toString().startsWith("showly2://trakt")) {
          (it as? OnTraktAuthorizeListener)?.onAuthorizationResult(authData)
        }
      }
    }
  }
}
