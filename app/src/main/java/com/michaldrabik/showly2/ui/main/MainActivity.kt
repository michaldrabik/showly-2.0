package com.michaldrabik.showly2.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.michaldrabik.common.Config
import com.michaldrabik.common.Mode
import com.michaldrabik.common.Mode.MOVIES
import com.michaldrabik.common.Mode.SHOWS
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.showly2.BuildConfig
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ui.BaseActivity
import com.michaldrabik.showly2.ui.main.delegates.BillingDelegate
import com.michaldrabik.showly2.ui.main.delegates.MainBillingDelegate
import com.michaldrabik.showly2.ui.main.delegates.MainUpdateDelegate
import com.michaldrabik.showly2.ui.main.delegates.UpdateDelegate
import com.michaldrabik.showly2.ui.views.WhatsNewView
import com.michaldrabik.showly2.utilities.deeplink.DeepLinkResolver
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.common.OnShowsMoviesSyncedListener
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.events.Event
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.ShowsMoviesSyncComplete
import com.michaldrabik.ui_base.events.TraktQuickSyncSuccess
import com.michaldrabik.ui_base.events.TraktSyncAuthError
import com.michaldrabik.ui_base.network.NetworkStatusProvider
import com.michaldrabik.ui_base.sync.ShowsMoviesSyncWorker
import com.michaldrabik.ui_base.utilities.ModeHost
import com.michaldrabik.ui_base.utilities.MoviesStatusHost
import com.michaldrabik.ui_base.utilities.NavigationHost
import com.michaldrabik.ui_base.utilities.SnackbarHost
import com.michaldrabik.ui_base.utilities.TipsHost
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Tip
import com.michaldrabik.ui_model.Tip.MENU_DISCOVER
import com.michaldrabik.ui_model.Tip.MENU_MODES
import com.michaldrabik.ui_model.Tip.MENU_MY_SHOWS
import com.michaldrabik.ui_settings.helpers.AppLanguage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_bottom_menu.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity :
  BaseActivity(),
  SnackbarHost,
  NavigationHost,
  TipsHost,
  ModeHost,
  MoviesStatusHost,
  UpdateDelegate by MainUpdateDelegate(),
  BillingDelegate by MainBillingDelegate() {

  companion object {
    private const val NAVIGATION_TRANSITION_DURATION_MS = 350L
    private const val ARG_NAVIGATION_VISIBLE = "ARG_NAVIGATION_VISIBLE"
  }

  private val viewModel by viewModels<MainViewModel>()

  private val navigationHeightPad by lazy { dimenToPx(R.dimen.bottomNavigationHeightPadded) }
  private val navigationHeight by lazy { dimenToPx(R.dimen.bottomNavigationHeight) }
  private val decelerateInterpolator by lazy { DecelerateInterpolator(2F) }
  private val tips by lazy {
    mapOf(
      MENU_DISCOVER to tutorialTipDiscover, MENU_MY_SHOWS to tutorialTipMyShows, MENU_MODES to tutorialTipModeMenu
    )
  }

  @Inject lateinit var workManager: WorkManager
  @Inject lateinit var eventsManager: EventsManager
  @Inject lateinit var deepLinkResolver: DeepLinkResolver
  @Inject lateinit var settingsRepository: SettingsRepository
  @Inject lateinit var networkStatusProvider: NetworkStatusProvider

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    registerBilling(this, settingsRepository)
    registerUpdate(this) { onUpdateDownloaded(it) }

    setupViewModel()
    setupNavigation()
    setupTips()
    setupView()
    setupNetworkObserver()

    restoreState(savedInstanceState)
    onNewIntent(intent)
  }

  override fun onStart() {
    super.onStart()
    ShowsMoviesSyncWorker.schedule(workManager)
  }

  override fun onResume() {
    super.onResume()
    setupBackPressed()
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    handleAppShortcut(intent)
    handleNotification(intent?.extras) { hideNavigation(false) }
    handleTraktAuthorization(intent?.data)
    handleDeepLink(intent)
  }

  override fun onDestroy() {
    lifecycle.removeObserver(networkStatusProvider)
    super.onDestroy()
  }

  private fun setupViewModel() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch { viewModel.uiState.collect { render(it) } }
        launch { eventsManager.events.collect { handleEvent(it) } }
      }
    }
    viewModel.initialize()
    viewModel.refreshTraktSyncSchedule()
  }

  private fun setupView() {
    with(bottomMenuView) {
      isModeMenuEnabled = hasMoviesEnabled()
      onModeSelected = { setMode(it) }
    }
    viewMask.onClick { /* NOOP */ }
  }

  private fun setupNetworkObserver() {
    lifecycle.addObserver(networkStatusProvider)
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          networkStatusProvider.status.collect {
            statusView.visibleIf(!it)
            statusView.text = getString(R.string.errorNoInternetConnection)
          }
        }
      }
    }
  }

  private fun setupNavigation() {
    findNavControl()?.run {
      val graph = navInflater.inflate(R.navigation.navigation_graph).apply {
        val destination = when (viewModel.getMode()) {
          SHOWS -> R.id.progressMainFragment
          MOVIES -> R.id.progressMoviesMainFragment
          else -> throw IllegalStateException()
        }
        setStartDestination(destination)
      }
      setGraph(graph, Bundle.EMPTY)
    }
    with(bottomNavigationView) {
      setOnItemSelectedListener { item ->
        if (selectedItemId == item.itemId) {
          doForFragments { (it as? OnTabReselectedListener)?.onTabReselected() }
          return@setOnItemSelectedListener true
        }

        val target = when (item.itemId) {
          R.id.menuProgress -> getMenuProgressAction()
          R.id.menuDiscover -> getMenuDiscoverAction()
          R.id.menuCollection -> getMenuCollectionAction()
          R.id.menuNews -> R.id.actionNavigateNewsFragment
          else -> throw IllegalStateException("Invalid menu item.")
        }

        findNavControl()?.navigate(target)
        showNavigation(true)

        if (item.itemId == R.id.menuDiscover) {
          // Try showing rate app dialog when user navigates to Discover section.
          viewModel.checkRateApp()
        }

        return@setOnItemSelectedListener true
      }
      menu.findItem(R.id.menuNews).isVisible = viewModel.hasNewsEnabled()
    }
  }

  private fun setupBackPressed() {
    onBackPressedDispatcher.addCallback(this) {
      if (tutorialView.isVisible) {
        tutorialView.fadeOut()
        return@addCallback
      }
      findNavControl()?.run {
        when (currentDestination?.id) {
          R.id.discoverFragment,
          R.id.discoverMoviesFragment,
          R.id.followedShowsFragment,
          R.id.followedMoviesFragment,
          R.id.listsFragment,
          R.id.newsFragment,
          -> {
            bottomNavigationView.selectedItemId = R.id.menuProgress
          }
          else -> {
            remove()
            super.onBackPressed()
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
    setTipShow(tip)
  }

  override fun setTipShow(tip: Tip) = viewModel.setTipShown(tip)

  override fun isTipShown(tip: Tip) = viewModel.isTipShown(tip)

  override fun hideNavigation(animate: Boolean) {
    bottomNavigationView.run {
      isEnabled = false
      isClickable = false
    }
    tips.values.forEach { it.gone() }
    snackbarHost.translationY = navigationHeight.toFloat()
    bottomNavigationWrapper.animate().translationYBy(navigationHeightPad.toFloat()).setDuration(if (animate) NAVIGATION_TRANSITION_DURATION_MS else 0)
      .setInterpolator(decelerateInterpolator).start()
  }

  override fun showNavigation(animate: Boolean) {
    bottomNavigationView.run {
      isEnabled = true
      isClickable = true
    }
    tips.entries.forEach { (tip, view) -> view.visibleIf(!isTipShown(tip)) }
    snackbarHost.translationY = 0F
    bottomNavigationWrapper.animate().translationY(0F).setDuration(if (animate) NAVIGATION_TRANSITION_DURATION_MS else 0)
      .setInterpolator(decelerateInterpolator).start()
  }

  override fun navigateToDiscover() {
    bottomNavigationView.selectedItemId = R.id.menuDiscover
  }

  override fun setMode(mode: Mode, force: Boolean) {
    if (force || viewModel.getMode() != mode) {
      viewModel.setMode(mode)
      val target = when (bottomNavigationView.selectedItemId) {
        R.id.menuDiscover -> getMenuDiscoverAction()
        R.id.menuCollection -> getMenuCollectionAction()
        R.id.menuProgress -> getMenuProgressAction()
        R.id.menuNews -> R.id.actionNavigateNewsFragment
        else -> 0
      }
      if (target != 0) {
        findNavControl()?.navigate(target)
      }
    }
  }

  override fun getMode() = viewModel.getMode()

  override fun hasMoviesEnabled() = viewModel.hasMoviesEnabled()

  private fun render(uiState: MainUiState) {
    uiState.run {
      isLoading.let {
        mainProgress.visibleIf(it)
      }
      showMask.let {
        viewMask.visibleIf(it)
      }
      isInitialRun?.let {
        if (it.consume() == true) {
          viewModel.checkInitialLanguage()
        }
      }
      showWhatsNew?.let {
        if (it.consume() == true) showWhatsNewDialog()
      }
      showRateApp?.let {
        if (it.consume() == true) {
          launchInAppReview()
          Analytics.logInAppRateDisplayed()
        }
      }
      initialLanguage?.let { event ->
        event.consume()?.let {
          showWelcomeDialog(it)
        }
      }
      openLink?.let { event ->
        event.consume()?.let { bundle ->
          findNavHostFragment()?.findNavController()?.let { nav ->
            bundle.show?.let {
              deepLinkResolver.resolveDestination(nav, bottomNavigationView, it)
            }
            bundle.movie?.let {
              deepLinkResolver.resolveDestination(nav, bottomNavigationView, it)
            }
          }
        }
      }
    }
  }

  private fun showWelcomeDialog(language: AppLanguage) {
    navigateToDiscover()
    with(welcomeView) {
      setLanguage(language)
      fadeIn()
      onOkClickListener = {
        fadeOut()
        showMask(false)
        if (language != AppLanguage.ENGLISH) {
          showWelcomeLanguageDialog(language)
        }
      }
    }
    showMask(true)
  }

  private fun showWelcomeLanguageDialog(language: AppLanguage) {
    with(welcomeLanguageView) {
      setLanguage(language)
      fadeIn()
      onYesClick = {
        viewModel.setLanguage(language)
        fadeOut()
        showMask(false)
      }
      onNoClick = {
        viewModel.setLanguage(AppLanguage.ENGLISH)
        fadeOut()
        showMask(false)
      }
    }
    showMask(true)
  }

  private fun showMask(show: Boolean) {
    viewMask.visibleIf(show)
    if (!show) viewModel.clearMask()
  }

  private fun launchInAppReview() {
    val manager = ReviewManagerFactory.create(applicationContext)
    val request = manager.requestReviewFlow()
    request.addOnCompleteListener {
      if (it.isSuccessful) {
        val flow = manager.launchReviewFlow(this@MainActivity, it.result)
        flow.addOnCompleteListener { viewModel.completeAppRate() }
      }
    }
  }

  @SuppressLint("MissingSuperCall")
  override fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(ARG_NAVIGATION_VISIBLE, bottomNavigationWrapper.translationY == 0F)
    super.onSaveInstanceState(outState)
  }

  private fun restoreState(savedInstanceState: Bundle?) {
    val isNavigationVisible = savedInstanceState?.getBoolean(ARG_NAVIGATION_VISIBLE, true) ?: true
    if (!isNavigationVisible) hideNavigation(true)
  }

  private fun doForFragments(action: (Fragment) -> Unit) {
    findNavControl()?.currentDestination?.id?.let {
      val navHost = supportFragmentManager.findFragmentById(R.id.navigationHost)
      navHost?.childFragmentManager?.primaryNavigationFragment?.let { action(it) }
    }
  }

  private fun handleEvent(event: Event) {
    when (event) {
      is ShowsMoviesSyncComplete -> {
        if (event.count > 0) {
          doForFragments { (it as? OnShowsMoviesSyncedListener)?.onShowsMoviesSyncFinished() }
        }
        viewModel.refreshAnnouncements()
      }
      is TraktQuickSyncSuccess -> {
        val message = resources.getQuantityString(R.plurals.textTraktQuickSyncComplete, event.count, event.count)
        provideSnackbarLayout().showInfoSnackbar(message)
      }
      is TraktSyncAuthError -> {
        provideSnackbarLayout().showErrorSnackbar(getString(R.string.errorTraktAuthorization))
      }
      else -> Timber.d("Event ignored. Noop.")
    }
  }

  private fun handleAppShortcut(intent: Intent?) {
    when {
      intent == null -> return
      intent.extras?.containsKey("extraShortcutProgress") == true -> bottomNavigationView.selectedItemId = R.id.menuProgress
      intent.extras?.containsKey("extraShortcutDiscover") == true -> bottomNavigationView.selectedItemId = R.id.menuDiscover
      intent.extras?.containsKey("extraShortcutCollection") == true -> bottomNavigationView.selectedItemId = R.id.menuCollection
      intent.extras?.containsKey("extraShortcutSearch") == true -> {
        bottomNavigationView.selectedItemId = R.id.menuDiscover
        val action = when (viewModel.getMode()) {
          SHOWS -> R.id.actionDiscoverFragmentToSearchFragment
          MOVIES -> R.id.actionDiscoverMoviesFragmentToSearchFragment
          else -> throw IllegalStateException()
        }
        findNavControl()?.navigate(action)
      }
    }
  }

  private fun showWhatsNewDialog() {
    MaterialAlertDialogBuilder(this, R.style.AlertDialog).setBackground(ContextCompat.getDrawable(this, R.drawable.bg_dialog))
      .setView(WhatsNewView(this)).setCancelable(false).setPositiveButton(R.string.textClose) { _, _ -> }
      .setNeutralButton("Twitter") { _, _ -> openWebUrl(Config.TWITTER_URL) }.show()
  }

  private fun getMenuDiscoverAction() = when (viewModel.getMode()) {
    SHOWS -> R.id.actionNavigateDiscoverFragment
    MOVIES -> R.id.actionNavigateDiscoverMoviesFragment
    else -> throw IllegalStateException()
  }

  private fun getMenuCollectionAction() = when (viewModel.getMode()) {
    SHOWS -> R.id.actionNavigateFollowedShowsFragment
    MOVIES -> R.id.actionNavigateFollowedMoviesFragment
    else -> throw IllegalStateException()
  }

  private fun getMenuProgressAction() = when (viewModel.getMode()) {
    SHOWS -> R.id.actionNavigateProgressFragment
    MOVIES -> R.id.actionNavigateProgressMoviesFragment
    else -> throw IllegalStateException()
  }

  private fun onUpdateDownloaded(manager: AppUpdateManager) {
    provideSnackbarLayout().showInfoSnackbar(
      message = getString(R.string.textUpdateDownloaded), actionText = R.string.textUpdateInstall, length = LENGTH_INDEFINITE
    ) {
      Analytics.logInAppUpdate(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE.toLong())
      manager.completeUpdate()
    }
  }

  private fun handleDeepLink(intent: Intent?) {
    deepLinkResolver.findSource(intent)?.let {
      viewModel.openDeepLink(it)
    }
  }

  override fun findNavControl() = findNavHostFragment()?.findNavController()

  override fun provideSnackbarLayout(): ViewGroup = snackbarHost
}
