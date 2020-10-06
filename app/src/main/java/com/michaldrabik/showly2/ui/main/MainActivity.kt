package com.michaldrabik.showly2.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.review.ReviewManagerFactory
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.di.DaggerViewModelFactory
import com.michaldrabik.showly2.di.component.FragmentComponent
import com.michaldrabik.showly2.ui.NotificationActivity
import com.michaldrabik.showly2.ui.common.OnEpisodesSyncedListener
import com.michaldrabik.showly2.ui.common.views.WhatsNewView
import com.michaldrabik.showly2.utilities.extensions.*
import com.michaldrabik.showly2.utilities.network.NetworkObserver
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.NavigationHost
import com.michaldrabik.ui_base.SnackbarHost
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.events.*
import com.michaldrabik.ui_discover.di.UiDiscoverComponent
import com.michaldrabik.ui_discover.di.UiDiscoverComponentProvider
import com.michaldrabik.ui_model.Tip
import com.michaldrabik.ui_model.Tip.MENU_DISCOVER
import com.michaldrabik.ui_model.Tip.MENU_MY_SHOWS
import com.michaldrabik.ui_my_shows.di.UiMyShowsComponent
import com.michaldrabik.ui_my_shows.di.UiMyShowsComponentProvider
import com.michaldrabik.ui_search.di.UiSearchComponentProvider
import com.michaldrabik.ui_settings.di.UiSettingsComponent
import com.michaldrabik.ui_settings.di.UiSettingsComponentProvider
import com.michaldrabik.ui_show.di.UiShowDetailsComponent
import com.michaldrabik.ui_show.di.UiShowDetailsComponentProvider
import com.michaldrabik.ui_show.episode_details.di.UiEpisodeDetailsComponent
import com.michaldrabik.ui_show.episode_details.di.UiEpisodeDetailsComponentProvider
import com.michaldrabik.ui_show.gallery.di.UiFanartGalleryComponent
import com.michaldrabik.ui_show.gallery.di.UiFanartGalleryComponentProvider
import com.michaldrabik.ui_statistics.di.UiSearchComponent
import com.michaldrabik.ui_statistics.di.UiStatisticsComponent
import com.michaldrabik.ui_statistics.di.UiStatisticsComponentProvider
import com.michaldrabik.ui_trakt_sync.di.UiTraktSyncComponent
import com.michaldrabik.ui_trakt_sync.di.UiTraktSyncComponentProvider
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : NotificationActivity(),
  EventObserver,
  NetworkObserver,
  SnackbarHost,
  NavigationHost,
  UiTraktSyncComponentProvider,
  UiStatisticsComponentProvider,
  UiDiscoverComponentProvider,
  UiShowDetailsComponentProvider,
  UiFanartGalleryComponentProvider,
  UiEpisodeDetailsComponentProvider,
  UiMyShowsComponentProvider,
  UiSearchComponentProvider,
  UiSettingsComponentProvider {

  companion object {
    private const val NAVIGATION_TRANSITION_DURATION_MS = 350L
    private const val ARG_NAVIGATION_VISIBLE = "ARG_NAVIGATION_VISIBLE"
  }

  lateinit var fragmentComponent: FragmentComponent
  private lateinit var uiSettingsComponent: UiSettingsComponent
  private lateinit var uiTraktSyncComponent: UiTraktSyncComponent
  private lateinit var uiShowDetailsComponent: UiShowDetailsComponent
  private lateinit var uiShowGalleryComponent: UiFanartGalleryComponent
  private lateinit var uiEpisodeDetailsComponent: UiEpisodeDetailsComponent
  private lateinit var uiSearchComponent: UiSearchComponent
  private lateinit var uiDiscoverComponent: UiDiscoverComponent
  private lateinit var uiStatisticsComponent: UiStatisticsComponent
  private lateinit var uiMyShowsComponent: UiMyShowsComponent

  @Inject
  lateinit var viewModelFactory: DaggerViewModelFactory
  private lateinit var viewModel: MainViewModel

  private val navigationHeight by lazy { dimenToPx(R.dimen.bottomNavigationHeightPadded) }
  private val decelerateInterpolator by lazy { DecelerateInterpolator(2F) }
  private val tips by lazy {
    mapOf(
      MENU_DISCOVER to tutorialTipDiscover,
      MENU_MY_SHOWS to tutorialTipMyShows
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    setupComponents()
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    setupViewModel()
    setupNavigation()
    setupNavigationBackHandler()
    setupTips()
    setupView()

    restoreState(savedInstanceState)
    onNewIntent(intent)
  }

  private fun setupComponents() {
    appComponent().inject(this)
    fragmentComponent = appComponent().fragmentComponent().create()
    uiSettingsComponent = appComponent().uiSettingsComponent().create()
    uiTraktSyncComponent = appComponent().uiTraktSyncComponent().create()
    uiShowDetailsComponent = appComponent().uiShowDetailsComponent().create()
    uiSearchComponent = appComponent().uiSearchComponent().create()
    uiStatisticsComponent = appComponent().uiStatisticsComponent().create()
    uiShowGalleryComponent = appComponent().uiShowGalleryComponent().create()
    uiEpisodeDetailsComponent = appComponent().uiEpisodeDetailsComponent().create()
    uiDiscoverComponent = appComponent().uiDiscoverComponent().create()
    uiMyShowsComponent = appComponent().uiMyShowsComponent().create()
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    handleAppShortcut(intent)
    handleNotification(intent?.extras) { hideNavigation(false) }
    handleTraktAuthorization(intent?.data)
  }

  override fun onStop() {
    viewModel.clearUp()
    super.onStop()
  }

  private fun setupViewModel() {
    viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    viewModel.run {
      uiLiveData.observe(this@MainActivity) { render(it!!) }
      initSettings()
      refreshTraktSyncSchedule(applicationContext)
    }
  }

  private fun setupView() {
    rateAppView.onYesClickListener = {
      rateAppView.fadeOut()
      val manager = ReviewManagerFactory.create(applicationContext)
      val request = manager.requestReviewFlow()
      request.addOnCompleteListener {
        if (it.isSuccessful) {
          val flow = manager.launchReviewFlow(this, it.result)
          flow.addOnCompleteListener { viewModel.finishRateApp() }
        }
      }
      Analytics.logInAppRateDecision(true)
    }
    rateAppView.onNoClickListener = {
      rateAppView.fadeOut()
      Analytics.logInAppRateDecision(false)
    }
  }

  private fun setupNavigation() {
    bottomNavigationView.setOnNavigationItemSelectedListener { item ->
      if (bottomNavigationView.selectedItemId == item.itemId) {
        doForFragments { (it as? OnTabReselectedListener)?.onTabReselected() }
        return@setOnNavigationItemSelectedListener true
      }

      val target = when (item.itemId) {
        R.id.menuWatchlist -> R.id.actionNavigateWatchlistFragment
        R.id.menuDiscover -> R.id.actionNavigateDiscoverFragment
        R.id.menuShows -> R.id.actionNavigateFollowedShowsFragment
        else -> throw IllegalStateException("Invalid menu item.")
      }

      clearUiCache()
      navigationHost.findNavController().navigate(target)
      showNavigation(true)
      return@setOnNavigationItemSelectedListener true
    }
  }

  private fun setupNavigationBackHandler() {
    onBackPressedDispatcher.addCallback(this) {
      if (tutorialView.isVisible) {
        tutorialView.fadeOut()
        return@addCallback
      }

      navigationHost.findNavController().run {
        if (currentDestination?.id == R.id.watchlistFragment) {
          remove()
          super.onBackPressed()
        }
        when (currentDestination?.id) {
          R.id.discoverFragment, R.id.followedShowsFragment -> {
            bottomNavigationView.selectedItemId = R.id.menuWatchlist
          }
        }
      }
    }
  }

  private fun setupTips() {
    tips.entries.forEach { (tip, view) ->
      view.visibleIf(!isTipShown(tip))
      view.onClick {
        it.gone()
        showTip(tip)
      }
    }
  }

  fun showTip(tip: Tip) {
    tutorialView.showTip(tip)
    viewModel.setTipShown(tip)
  }

  fun isTipShown(tip: Tip) = viewModel.isTipShown(tip)

  override fun hideNavigation(animate: Boolean) {
    bottomNavigationView.run {
      isEnabled = false
      isClickable = false
    }
    tips.values.forEach { it.gone() }
    bottomNavigationWrapper.animate()
      .translationYBy(navigationHeight.toFloat())
      .setDuration(if (animate) NAVIGATION_TRANSITION_DURATION_MS else 0)
      .setInterpolator(decelerateInterpolator)
      .start()
  }

  override fun showNavigation(animate: Boolean) {
    bottomNavigationView.run {
      isEnabled = true
      isClickable = true
    }
    tips.entries.forEach { (tip, view) -> view.visibleIf(!isTipShown(tip)) }
    bottomNavigationWrapper.animate()
      .translationY(0F)
      .setDuration(if (animate) NAVIGATION_TRANSITION_DURATION_MS else 0)
      .setInterpolator(decelerateInterpolator)
      .start()
  }

  fun openTab(@IdRes navigationId: Int) {
    bottomNavigationView.selectedItemId = navigationId
  }

  private fun render(uiModel: MainUiModel) {
    uiModel.run {
      isInitialRun?.let { if (it) openTab(R.id.menuDiscover) }
      showWhatsNew?.let { if (it) showWhatsNew() }
      showRateApp?.let {
        rateAppView.fadeIf(it, startDelay = 1500)
        if (it) Analytics.logInAppRateDisplayed()
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(ARG_NAVIGATION_VISIBLE, bottomNavigationWrapper.translationY == 0F)
    super.onSaveInstanceState(outState)
  }

  private fun restoreState(savedInstanceState: Bundle?) {
    val isNavigationVisible = savedInstanceState?.getBoolean(ARG_NAVIGATION_VISIBLE, true) ?: true
    if (!isNavigationVisible) hideNavigation(true)
  }

  private fun doForFragments(action: (Fragment) -> Unit) {
    navigationHost.findNavController().currentDestination?.id?.let {
      val navHost = supportFragmentManager.findFragmentById(R.id.navigationHost)
      navHost?.childFragmentManager?.primaryNavigationFragment?.let {
        action(it)
      }
    }
  }

  override fun onNetworkAvailableListener(isAvailable: Boolean) =
    runOnUiThread {
      statusView.visibleIf(!isAvailable)
      statusView.text = getString(R.string.errorNoInternetConnection)
    }

  override fun onNewEvent(event: Event) {
    runOnUiThread {
      when (event) {
        is ShowsSyncComplete -> {
          doForFragments { (it as? OnEpisodesSyncedListener)?.onEpisodesSyncFinished() }
          viewModel.refreshAnnouncements(applicationContext)
        }
        is TraktSyncProgress -> {
          doForFragments { (it as? OnTraktSyncListener)?.onTraktSyncProgress() }
        }
        is TraktQuickSyncSuccess -> {
          val text = resources.getQuantityString(R.plurals.textTraktQuickSyncComplete, event.count, event.count)
          snackBarHost.showInfoSnackbar(text)
        }
      }
    }
  }

  private fun handleAppShortcut(intent: Intent?) {
    when {
      intent == null -> return
      intent.extras?.containsKey("extraShortcutWatchlist") == true ->
        bottomNavigationView.selectedItemId = R.id.menuWatchlist
      intent.extras?.containsKey("extraShortcutDiscover") == true ->
        bottomNavigationView.selectedItemId = R.id.menuDiscover
      intent.extras?.containsKey("extraShortcutMyShows") == true ->
        bottomNavigationView.selectedItemId = R.id.menuShows
      intent.extras?.containsKey("extraShortcutSearch") == true -> {
        bottomNavigationView.selectedItemId = R.id.menuDiscover
        navigationHost.findNavController().navigate(R.id.actionDiscoverFragmentToSearchFragment)
      }
    }
  }

  private fun showWhatsNew() {
    MaterialAlertDialogBuilder(this, R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(this, R.drawable.bg_dialog))
      .setView(WhatsNewView(this))
      .setCancelable(false)
      .setPositiveButton(R.string.textClose) { _, _ -> }
      .show()
  }

  override fun provideSnackbarLayout(): ViewGroup = snackBarHost

  override fun provideSettingsComponent() = uiSettingsComponent
  override fun provideTraktSyncComponent() = uiTraktSyncComponent
  override fun provideShowDetailsComponent() = uiShowDetailsComponent
  override fun provideStatisticsComponent() = uiStatisticsComponent
  override fun provideSearchComponent() = uiSearchComponent
  override fun provideEpisodeDetailsComponent() = uiEpisodeDetailsComponent
  override fun provideFanartGalleryComponent() = uiShowGalleryComponent
  override fun provideMyShowsComponent() = uiMyShowsComponent
  override fun provideDiscoverComponent() = uiDiscoverComponent
}
