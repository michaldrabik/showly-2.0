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
import com.michaldrabik.showly2.ui.BaseActivity
import com.michaldrabik.showly2.ui.views.WhatsNewView
import com.michaldrabik.showly2.utilities.NetworkObserver
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.common.OnEpisodesSyncedListener
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.common.OnTranslationsSyncListener
import com.michaldrabik.ui_base.events.Event
import com.michaldrabik.ui_base.events.EventObserver
import com.michaldrabik.ui_base.events.ShowsSyncComplete
import com.michaldrabik.ui_base.events.TraktQuickSyncSuccess
import com.michaldrabik.ui_base.events.TraktSyncProgress
import com.michaldrabik.ui_base.events.TranslationsSyncProgress
import com.michaldrabik.ui_base.utilities.Mode
import com.michaldrabik.ui_base.utilities.Mode.*
import com.michaldrabik.ui_base.utilities.NavigationHost
import com.michaldrabik.ui_base.utilities.SnackbarHost
import com.michaldrabik.ui_base.utilities.TipsHost
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Tip
import com.michaldrabik.ui_model.Tip.MENU_DISCOVER
import com.michaldrabik.ui_model.Tip.MENU_MY_SHOWS
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity :
  BaseActivity(),
  EventObserver,
  NetworkObserver,
  SnackbarHost,
  NavigationHost,
  TipsHost {

  companion object {
    private const val NAVIGATION_TRANSITION_DURATION_MS = 350L
    private const val ARG_NAVIGATION_VISIBLE = "ARG_NAVIGATION_VISIBLE"
  }

  @Inject
  lateinit var viewModelFactory: DaggerViewModelFactory
  private lateinit var viewModel: MainViewModel

  private var mode = SHOWS
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

  override fun setupComponents() {
    appComponent().inject(this)
    super.setupComponents()
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
      initialize()
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
        R.id.menuProgress -> R.id.actionNavigateProgressFragment
        R.id.menuDiscover -> getMenuDiscoverAction()
        R.id.menuCollection -> R.id.actionNavigateFollowedShowsFragment
        else -> throw IllegalStateException("Invalid menu item.")
      }

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
        if (currentDestination?.id == R.id.progressFragment) {
          remove()
          super.onBackPressed()
        }
        when (currentDestination?.id) {
          R.id.discoverFragment,
          R.id.discoverMoviesFragment,
          R.id.followedShowsFragment -> {
            bottomNavigationView.selectedItemId = R.id.menuProgress
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

  override fun showTip(tip: Tip) {
    tutorialView.showTip(tip)
    viewModel.setTipShown(tip)
  }

  override fun isTipShown(tip: Tip) = viewModel.isTipShown(tip)

  private fun getMenuDiscoverAction() =
    if (mode == MOVIES) R.id.actionNavigateDiscoverMoviesFragment
    else R.id.actionNavigateDiscoverFragment

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

  override fun openTab(@IdRes navigationId: Int) {
    bottomNavigationView.selectedItemId = navigationId
  }

  override fun openDiscoverTab() = openTab(R.id.menuDiscover)

  override fun setMode(mode: Mode) {
    if (this.mode != mode) viewModel.setMode(mode)
  }

  private fun render(uiModel: MainUiModel) {
    uiModel.run {
      isInitialRun?.let {
        if (it) openTab(R.id.menuDiscover)
      }
      mode?.let {
        if (this@MainActivity.mode != it) {
          this@MainActivity.mode = it
          if (bottomNavigationView.selectedItemId == R.id.menuDiscover) {
            val target = getMenuDiscoverAction()
            navigationHost.findNavController().navigate(target)
          }
        }
      }
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
    navigationHost?.findNavController()?.currentDestination?.id?.let {
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
        is TranslationsSyncProgress -> {
          doForFragments { (it as? OnTranslationsSyncListener)?.onTranslationsSyncProgress() }
        }
        is TraktSyncProgress -> {
          doForFragments { (it as? OnTraktSyncListener)?.onTraktSyncProgress() }
        }
        is TraktQuickSyncSuccess -> {
          val text = resources.getQuantityString(R.plurals.textTraktQuickSyncComplete, event.count, event.count)
          snackBarHost.showInfoSnackbar(text)
        }
        else -> Timber.d("Event ignored. Noop.")
      }
    }
  }

  private fun handleAppShortcut(intent: Intent?) {
    when {
      intent == null -> return
      intent.extras?.containsKey("extraShortcutProgress") == true ->
        bottomNavigationView.selectedItemId = R.id.menuProgress
      intent.extras?.containsKey("extraShortcutDiscover") == true ->
        bottomNavigationView.selectedItemId = R.id.menuDiscover
      intent.extras?.containsKey("extraShortcutMyShows") == true ->
        bottomNavigationView.selectedItemId = R.id.menuCollection
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
}
