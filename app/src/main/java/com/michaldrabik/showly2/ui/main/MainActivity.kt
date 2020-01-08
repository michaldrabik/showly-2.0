package com.michaldrabik.showly2.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.common.ShowsSyncService
import com.michaldrabik.showly2.common.trakt.TraktImportService
import com.michaldrabik.showly2.connectivityManager
import com.michaldrabik.showly2.di.DaggerViewModelFactory
import com.michaldrabik.showly2.ui.NotificationActivity
import com.michaldrabik.showly2.ui.common.OnEpisodesSyncedListener
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.OnTraktImportListener
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import com.michaldrabik.showly2.utilities.network.NetworkMonitor
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : NotificationActivity() {

  companion object {
    private const val NAVIGATION_TRANSITION_DURATION_MS = 350L
    private const val ARG_NAVIGATION_VISIBLE = "ARG_NAVIGATION_VISIBLE"
  }

  private val navigationHeight by lazy { dimenToPx(R.dimen.bottomNavigationHeightPadded) }
  private val decelerateInterpolator by lazy { DecelerateInterpolator(2F) }
  private val networkMonitor by lazy { NetworkMonitor(connectivityManager()) }

  @Inject lateinit var viewModelFactory: DaggerViewModelFactory
  private lateinit var viewModel: MainViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    setupViewModel()
    setupNavigation()
    setupNavigationBackHandler()
    setupNetworkMonitoring()
    setupTutorials()

    restoreState(savedInstanceState)
    onNewIntent(intent)
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    handleNotification(intent?.extras) { hideNavigation(false) }
    handleTraktAuthorization(intent?.data)
  }

  override fun onStart() {
    super.onStart()
    val filter = IntentFilter().apply {
      addAction(ShowsSyncService.ACTION_SHOWS_SYNC_FINISHED)
      addAction(TraktImportService.ACTION_IMPORT_PROGRESS)
    }
    LocalBroadcastManager.getInstance(applicationContext).registerReceiver(servicesReceiver, filter)
    ShowsSyncService.initialize(applicationContext)
  }

  override fun onStop() {
    LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(servicesReceiver)
    super.onStop()
  }

  private fun setupViewModel() {
    viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    viewModel.run {
      uiStream.observe(this@MainActivity, Observer { render(it!!) })
      initSettings()
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

      viewModel.clearCache()
      navigationHost.findNavController().navigate(target)
      showNavigation()
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

  private fun setupNetworkMonitoring() {
    networkMonitor.onNetworkAvailableCallback = { isAvailable ->
      runOnUiThread { noInternetView.visibleIf(!isAvailable) }
    }
    lifecycle.addObserver(networkMonitor)
  }

  private fun setupTutorials() {
    tutorialView.onOkClick = {
      tutorialView.fadeOut()
    }

    tutorialTipDiscover.onClick {
      it.gone()
      tutorialView.fadeIn()
      tutorialView.showTip(R.string.textTutorialDiscover)
    }
  }

  fun hideNavigation(animate: Boolean = true) {
    bottomNavigationWrapper.animate()
      .translationYBy(navigationHeight.toFloat())
      .setDuration(if (animate) NAVIGATION_TRANSITION_DURATION_MS else 0)
      .setInterpolator(decelerateInterpolator)
      .start()
  }

  fun showNavigation(animate: Boolean = true) {
    bottomNavigationWrapper.animate()
      .translationY(0F)
      .setDuration(if (animate) NAVIGATION_TRANSITION_DURATION_MS else 0)
      .setInterpolator(decelerateInterpolator)
      .start()
  }

  private fun render(uiModel: MainUiModel) {
    uiModel.run {
      isInitialRun?.let {
        if (it) bottomNavigationView.selectedItemId = R.id.menuDiscover
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(ARG_NAVIGATION_VISIBLE, bottomNavigationWrapper.translationY == 0F)
    super.onSaveInstanceState(outState)
  }

  private fun restoreState(savedInstanceState: Bundle?) {
    val isNavigationVisible = savedInstanceState?.getBoolean(ARG_NAVIGATION_VISIBLE, true) ?: true
    if (!isNavigationVisible) hideNavigation()
  }

  private fun doForFragments(action: (Fragment) -> Unit) {
    navigationHost.findNavController().currentDestination?.id?.let {
      val navHost = supportFragmentManager.findFragmentById(R.id.navigationHost)
      navHost?.childFragmentManager?.primaryNavigationFragment?.let {
        action(it)
      }
    }
  }

  private val servicesReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      when (intent?.action) {
        ShowsSyncService.ACTION_SHOWS_SYNC_FINISHED -> {
          doForFragments { (it as? OnEpisodesSyncedListener)?.onEpisodesSyncFinished() }
          viewModel.refreshAnnouncements(applicationContext)
        }
        // TODO Refactor into listening to DB changes and Coroutines Flows
        TraktImportService.ACTION_IMPORT_PROGRESS -> {
          doForFragments { (it as? OnTraktImportListener)?.onTraktImportProgress() }
        }
      }
    }
  }
}
