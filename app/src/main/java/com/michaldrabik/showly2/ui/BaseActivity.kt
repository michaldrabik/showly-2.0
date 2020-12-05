package com.michaldrabik.showly2.ui

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fcm.FcmExtra
import com.michaldrabik.ui_base.common.OnTraktAuthorizeListener
import com.michaldrabik.ui_discover.di.UiDiscoverComponentProvider
import com.michaldrabik.ui_discover_movies.di.UiDiscoverMoviesComponentProvider
import com.michaldrabik.ui_episodes.details.di.UiEpisodeDetailsComponentProvider
import com.michaldrabik.ui_gallery.di.UiFanartGalleryComponentProvider
import com.michaldrabik.ui_movie.di.UiMovieDetailsComponentProvider
import com.michaldrabik.ui_my_movies.di.UiMyMoviesComponentProvider
import com.michaldrabik.ui_my_shows.di.UiMyShowsComponentProvider
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_progress.di.UiProgressComponentProvider
import com.michaldrabik.ui_search.di.UiSearchComponentProvider
import com.michaldrabik.ui_settings.di.UiSettingsComponentProvider
import com.michaldrabik.ui_show.di.UiShowDetailsComponentProvider
import com.michaldrabik.ui_statistics.di.UiStatisticsComponentProvider
import com.michaldrabik.ui_trakt_sync.di.UiTraktSyncComponentProvider
import com.michaldrabik.ui_widgets.progress.ProgressWidgetProvider
import com.michaldrabik.ui_widgets.search.SearchWidgetProvider
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

abstract class BaseActivity :
  AppCompatActivity(),
  UiTraktSyncComponentProvider,
  UiStatisticsComponentProvider,
  UiDiscoverComponentProvider,
  UiDiscoverMoviesComponentProvider,
  UiShowDetailsComponentProvider,
  UiMovieDetailsComponentProvider,
  UiFanartGalleryComponentProvider,
  UiEpisodeDetailsComponentProvider,
  UiMyShowsComponentProvider,
  UiMyMoviesComponentProvider,
  UiProgressComponentProvider,
  UiSearchComponentProvider,
  UiSettingsComponentProvider {

  private val showActionKeys = arrayOf(
    FcmExtra.SHOW_ID.key,
    ProgressWidgetProvider.EXTRA_SHOW_ID
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
        val exception = Throwable(BaseActivity::class.simpleName, error)
        Timber.e(error)
        FirebaseCrashlytics.getInstance().recordException(exception)
      }
    }
  }

  private fun handleFcmShowPush(extras: Bundle, key: String, action: () -> Unit) {
    val showId = extras.getString(key)?.toLong() ?: -1
    val bundle = Bundle().apply { putLong(ARG_SHOW_ID, showId) }
    navigationHost.findNavController().run {
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
        val exception = Throwable(BaseActivity::class.simpleName, e)
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
