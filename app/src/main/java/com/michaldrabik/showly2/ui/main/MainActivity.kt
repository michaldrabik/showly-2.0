package com.michaldrabik.showly2.ui.main

import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.ViewModelFactory
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

  companion object {
    private const val NAVIGATION_TRANSITION_DURATION_MS = 400L
    private const val ARG_NAVIGATION_VISIBLE = "ARG_NAVIGATION_VISIBLE"
  }

  private val navigationHeight by lazy { dimenToPx(R.dimen.bottomNavigationHeightPadded) }
  private val decelerateInterpolator by lazy { DecelerateInterpolator(2F) }

  @Inject lateinit var viewModelFactory: ViewModelFactory
  private lateinit var viewModel: MainViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    setupViewModel()
    setupNavigation()
    setupNavigationBackHandler()

    restoreState(savedInstanceState)
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
        onTabReselected()
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

  fun hideNavigation() {
    bottomNavigationWrapper.animate()
      .translationYBy(navigationHeight.toFloat())
      .setDuration(NAVIGATION_TRANSITION_DURATION_MS)
      .setInterpolator(decelerateInterpolator)
      .start()
  }

  fun showNavigation() {
    bottomNavigationWrapper.animate()
      .translationY(0F)
      .setDuration(NAVIGATION_TRANSITION_DURATION_MS)
      .setInterpolator(decelerateInterpolator)
      .start()
  }

  private fun onTabReselected() {
    navigationHost.findNavController().currentDestination?.id?.let {
      val navHost = supportFragmentManager.findFragmentById(R.id.navigationHost)
      navHost?.childFragmentManager?.primaryNavigationFragment?.let {
        (it as? OnTabReselectedListener)?.onTabReselected()
      }
    }
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
}
