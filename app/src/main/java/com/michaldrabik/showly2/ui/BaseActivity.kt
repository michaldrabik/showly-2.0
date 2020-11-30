package com.michaldrabik.showly2.ui

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.fcm.FcmExtra
import com.michaldrabik.ui_base.common.OnTraktAuthorizeListener
import com.michaldrabik.ui_discover.di.UiDiscoverComponent
import com.michaldrabik.ui_discover.di.UiDiscoverComponentProvider
import com.michaldrabik.ui_discover_movies.di.UiDiscoverMoviesComponent
import com.michaldrabik.ui_discover_movies.di.UiDiscoverMoviesComponentProvider
import com.michaldrabik.ui_episodes.details.di.UiEpisodeDetailsComponent
import com.michaldrabik.ui_episodes.details.di.UiEpisodeDetailsComponentProvider
import com.michaldrabik.ui_gallery.di.UiFanartGalleryComponent
import com.michaldrabik.ui_gallery.di.UiFanartGalleryComponentProvider
import com.michaldrabik.ui_movie.di.UiMovieDetailsComponent
import com.michaldrabik.ui_movie.di.UiMovieDetailsComponentProvider
import com.michaldrabik.ui_my_shows.di.UiMyShowsComponent
import com.michaldrabik.ui_my_shows.di.UiMyShowsComponentProvider
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_progress.di.UiProgressComponent
import com.michaldrabik.ui_progress.di.UiProgressComponentProvider
import com.michaldrabik.ui_search.di.UiSearchComponentProvider
import com.michaldrabik.ui_settings.di.UiSettingsComponent
import com.michaldrabik.ui_settings.di.UiSettingsComponentProvider
import com.michaldrabik.ui_show.di.UiShowDetailsComponent
import com.michaldrabik.ui_show.di.UiShowDetailsComponentProvider
import com.michaldrabik.ui_statistics.di.UiSearchComponent
import com.michaldrabik.ui_statistics.di.UiStatisticsComponent
import com.michaldrabik.ui_statistics.di.UiStatisticsComponentProvider
import com.michaldrabik.ui_trakt_sync.di.UiTraktSyncComponent
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
  UiProgressComponentProvider,
  UiSearchComponentProvider,
  UiSettingsComponentProvider {

  private val showActionKeys = arrayOf(
    FcmExtra.SHOW_ID.key,
    ProgressWidgetProvider.EXTRA_SHOW_ID
  )

  private lateinit var uiDiscoverComponent: UiDiscoverComponent
  private lateinit var uiDiscoverMoviesComponent: UiDiscoverMoviesComponent
  private lateinit var uiEpisodeDetailsComponent: UiEpisodeDetailsComponent
  private lateinit var uiMyShowsComponent: UiMyShowsComponent
  private lateinit var uiSearchComponent: UiSearchComponent
  private lateinit var uiSettingsComponent: UiSettingsComponent
  private lateinit var uiShowDetailsComponent: UiShowDetailsComponent
  private lateinit var uiMovieDetailsComponent: UiMovieDetailsComponent
  private lateinit var uiShowGalleryComponent: UiFanartGalleryComponent
  private lateinit var uiStatisticsComponent: UiStatisticsComponent
  private lateinit var uiTraktSyncComponent: UiTraktSyncComponent
  private lateinit var uiProgressComponent: UiProgressComponent

  override fun provideDiscoverComponent() = uiDiscoverComponent
  override fun provideDiscoverMoviesComponent() = uiDiscoverMoviesComponent
  override fun provideEpisodeDetailsComponent() = uiEpisodeDetailsComponent
  override fun provideFanartGalleryComponent() = uiShowGalleryComponent
  override fun provideMyShowsComponent() = uiMyShowsComponent
  override fun provideSearchComponent() = uiSearchComponent
  override fun provideSettingsComponent() = uiSettingsComponent
  override fun provideShowDetailsComponent() = uiShowDetailsComponent
  override fun provideMovieDetailsComponent() = uiMovieDetailsComponent
  override fun provideStatisticsComponent() = uiStatisticsComponent
  override fun provideTraktSyncComponent() = uiTraktSyncComponent
  override fun provideProgressComponent() = uiProgressComponent

  protected open fun setupComponents() {
    uiDiscoverComponent = appComponent().uiDiscoverComponent().create()
    uiDiscoverMoviesComponent = appComponent().uiDiscoverMoviesComponent().create()
    uiEpisodeDetailsComponent = appComponent().uiEpisodeDetailsComponent().create()
    uiMyShowsComponent = appComponent().uiMyShowsComponent().create()
    uiSearchComponent = appComponent().uiSearchComponent().create()
    uiSettingsComponent = appComponent().uiSettingsComponent().create()
    uiShowDetailsComponent = appComponent().uiShowDetailsComponent().create()
    uiMovieDetailsComponent = appComponent().uiMovieDetailsComponent().create()
    uiShowGalleryComponent = appComponent().uiShowGalleryComponent().create()
    uiStatisticsComponent = appComponent().uiStatisticsComponent().create()
    uiTraktSyncComponent = appComponent().uiTraktSyncComponent().create()
    uiProgressComponent = appComponent().uiWatchlistComponent().create()
  }

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
