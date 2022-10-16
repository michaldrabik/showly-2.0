package com.michaldrabik.ui_progress.main

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateMargins
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnSearchClickListener
import com.michaldrabik.ui_base.common.OnShowsMoviesSyncedListener
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.common.sheets.context_menu.ContextMenuBottomSheet
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Operation
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Type
import com.michaldrabik.ui_base.common.views.exSearchLocalViewInput
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.disableUi
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.enableUi
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.hideKeyboard
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.navigateToSafe
import com.michaldrabik.ui_base.utilities.extensions.nextPage
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showKeyboard
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_episodes.details.EpisodeDetailsBottomSheet
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_EPISODE_TAB_SELECTED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_EPISODE_DETAILS
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_ITEM_MENU
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.main.adapters.ProgressMainAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_progress_main.*
import timber.log.Timber

@AndroidEntryPoint
class ProgressMainFragment :
  BaseFragment<ProgressMainViewModel>(R.layout.fragment_progress_main),
  OnShowsMoviesSyncedListener,
  OnTabReselectedListener {

  companion object {
    private const val TRANSLATION_DURATION = 225L
  }

  override val viewModel by viewModels<ProgressMainViewModel>()
  override val navigationId = R.id.progressMainFragment

  private var adapter: ProgressMainAdapter? = null

  private var searchViewTranslation = 0F
  private var tabsTranslation = 0F
  private var sideIconTranslation = 0F
  private var currentPage = 0
  private var isSearching = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    savedInstanceState?.let {
      searchViewTranslation = it.getFloat("ARG_SEARCH_POSITION")
      tabsTranslation = it.getFloat("ARG_TABS_POSITION")
      sideIconTranslation = it.getFloat("ARG_SIDE_ICON_POSITION")
      currentPage = it.getInt("ARG_PAGE")
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupPager()
    setupStatusBar()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.messageFlow.collect { showSnack(it) } },
      doAfterLaunch = { viewModel.loadProgress() }
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat("ARG_SEARCH_POSITION", progressMainSearchView?.translationY ?: 0F)
    outState.putFloat("ARG_TABS_POSITION", progressMainTabs?.translationY ?: 0F)
    outState.putFloat("ARG_SIDE_ICON_POSITION", progressMainSideIcons?.translationY ?: 0F)
    outState.putInt("ARG_PAGE", progressMainPager?.currentItem ?: 0)
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
  }

  override fun onPause() {
    enableUi()
    tabsTranslation = progressMainTabs.translationY
    searchViewTranslation = progressMainSearchView.translationY
    sideIconTranslation = progressMainSideIcons.translationY
    super.onPause()
  }

  override fun onDestroyView() {
    progressMainPager.removeOnPageChangeListener(pageChangeListener)
    progressMainPager.adapter = null
    adapter = null
    super.onDestroyView()
  }

  private fun setupView() {
    with(progressMainCalendarIcon) {
      visibleIf(currentPage == 1)
      onClick { toggleCalendarMode() }
    }
    with(progressMainSearchIcon) {
      onClick { if (!isSearching) enterSearch() else exitSearch() }
    }

    with(progressMainSearchView) {
      hint = getString(R.string.textSearchFor)
      settingsIconVisible = true
      traktIconVisible = true
      isClickable = false
      onClick { openMainSearch() }
      onSettingsClickListener = { openSettings() }
      onTraktClickListener = { navigateTo(R.id.actionProgressFragmentToTraktSyncFragment) }
    }

    with(progressMainSearchLocalView) {
      onCloseClickListener = { exitSearch() }
    }

    with(progressMainPagerModeTabs) {
      visibleIf(moviesEnabled)
      onModeSelected = { mode = it }
      selectShows()
    }

    progressMainTabs.translationY = tabsTranslation
    progressMainPagerModeTabs.translationY = tabsTranslation
    progressMainSearchView.translationY = searchViewTranslation
    progressMainSideIcons.translationY = sideIconTranslation
  }

  private fun setupPager() {
    adapter = ProgressMainAdapter(childFragmentManager, requireContext())
    progressMainPager.run {
      adapter = this@ProgressMainFragment.adapter
      offscreenPageLimit = ProgressMainAdapter.PAGES_COUNT
      addOnPageChangeListener(pageChangeListener)
    }
    progressMainTabs.setupWithViewPager(progressMainPager)
  }

  private fun setupStatusBar() {
    progressMainRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      val progressTabsMargin = if (moviesEnabled) R.dimen.progressSearchViewPadding else R.dimen.progressSearchViewPaddingNoModes
      val progressMainSearchLocalMargin = if (moviesEnabled) R.dimen.progressSearchLocalViewPadding else R.dimen.progressSearchLocalViewPaddingNoModes
      (progressMainSearchView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceMedium))
      (progressMainSearchLocalView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(progressMainSearchLocalMargin))
      (progressMainPagerModeTabs.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.collectionTabsMargin))
      arrayOf(progressMainTabs, progressMainSideIcons).forEach {
        val margin = statusBarSize + dimenToPx(progressTabsMargin)
        (it.layoutParams as ViewGroup.MarginLayoutParams).updateMargins(top = margin)
      }
    }
  }

  override fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      if (isSearching) {
        exitSearch()
      } else {
        isEnabled = false
        activity?.onBackPressed()
      }
    }
  }

  private fun openMainSearch() {
    disableUi()
    hideNavigation()
    progressMainPagerModeTabs.fadeOut(duration = 200).add(animations)
    progressMainTabs.fadeOut(duration = 200).add(animations)
    progressMainSideIcons.fadeOut(duration = 200).add(animations)
    progressMainPager.fadeOut(duration = 200) {
      super.navigateTo(R.id.actionProgressFragmentToSearch, null)
    }.add(animations)
  }

  fun openTraktSync() {
    hideNavigation()
    exitSearch()
    navigateTo(R.id.actionProgressFragmentToTraktSyncFragment)
  }

  fun openShowDetails(show: Show) {
    hideNavigation()
    progressMainRoot.fadeOut(150) {
      if (findNavControl()?.currentDestination?.id == R.id.progressMainFragment) {
        val bundle = Bundle().apply { putLong(ARG_SHOW_ID, show.traktId) }
        navigateTo(R.id.actionProgressFragmentToShowDetailsFragment, bundle)
        exitSearch()
      } else {
        showNavigation()
        progressMainRoot.fadeIn(50).add(animations)
      }
    }.add(animations)
  }

  fun openShowMenu(show: Show) {
    setFragmentResultListener(REQUEST_ITEM_MENU) { requestKey, _ ->
      if (requestKey == REQUEST_ITEM_MENU) {
        viewModel.loadProgress()
      }
      clearFragmentResultListener(REQUEST_ITEM_MENU)
    }
    val bundle = ContextMenuBottomSheet.createBundle(show.ids.trakt, showPinButtons = true)
    navigateToSafe(R.id.actionProgressFragmentToItemMenu, bundle)
  }

  fun openEpisodeDetails(show: Show, episode: Episode, season: Season) {
    setFragmentResultListener(REQUEST_EPISODE_DETAILS) { _, bundle ->
      when {
        bundle.containsKey(ACTION_EPISODE_TAB_SELECTED) -> {
          val selectedEpisode = bundle.getParcelable<Episode>(ACTION_EPISODE_TAB_SELECTED)!!
          openEpisodeDetails(show, selectedEpisode, season)
        }
      }
    }
    val bundle = EpisodeDetailsBottomSheet.createBundle(
      ids = show.ids,
      episode = episode,
      seasonEpisodesIds = null,
      isWatched = false,
      showButton = false,
      showTabs = true
    )
    navigateToSafe(R.id.actionProgressFragmentToEpisodeDetails, bundle)
  }

  fun openRateDialog(episodeBundle: EpisodeBundle) {
    setFragmentResultListener(NavigationArgs.REQUEST_RATING) { _, bundle ->
      when (bundle.getParcelable<Operation>(NavigationArgs.RESULT)) {
        Operation.SAVE -> showSnack(MessageEvent.Info(R.string.textRateSaved))
        Operation.REMOVE -> showSnack(MessageEvent.Info(R.string.textRateRemoved))
        else -> Timber.w("Unknown result")
      }
      viewModel.setWatchedEpisode(episodeBundle)
    }
    val bundle = RatingsBottomSheet.createBundle(episodeBundle.episode.ids.trakt, Type.EPISODE)
    navigateTo(R.id.actionProgressFragmentToRating, bundle)
  }

  private fun openSettings() {
    hideNavigation()
    exitSearch()
    navigateTo(R.id.actionProgressFragmentToSettingsFragment)
  }

  private fun enterSearch() {
    resetTranslations()
    progressMainSearchLocalView.fadeIn(150)
    with(exSearchLocalViewInput) {
      setText("")
      doAfterTextChanged { viewModel.onSearchQuery(it?.toString()) }
      visible()
      showKeyboard()
      requestFocus()
    }
    isSearching = true
    childFragmentManager.fragments.forEach { (it as? OnSearchClickListener)?.onEnterSearch() }
  }

  private fun exitSearch() {
    isSearching = false
    childFragmentManager.fragments.forEach { (it as? OnSearchClickListener)?.onExitSearch() }
    resetTranslations()
    progressMainSearchLocalView.gone()
    with(exSearchLocalViewInput) {
      setText("")
      gone()
      hideKeyboard()
      clearFocus()
    }
  }

  fun toggleCalendarMode() {
    exitSearch()
    onScrollReset()
    resetTranslations()
    viewModel.toggleCalendarMode()
  }

  override fun onShowsMoviesSyncFinished() = viewModel.loadProgress()

  override fun onTabReselected() {
    resetTranslations(duration = 0)
    progressMainPager?.nextPage()
    onScrollReset()
  }

  fun resetTranslations(duration: Long = TRANSLATION_DURATION) {
    if (view == null) return
    arrayOf(
      progressMainSearchView,
      progressMainTabs,
      progressMainPagerModeTabs,
      progressMainSideIcons,
      progressMainSearchLocalView
    ).forEach {
      it.animate().translationY(0F).setDuration(duration).add(animations)?.start()
    }
  }

  private fun onScrollReset() =
    childFragmentManager.fragments.forEach { (it as? OnScrollResetListener)?.onScrollReset() }

  private fun render(uiState: ProgressMainUiState) {
    progressMainSearchView.setTraktProgress(uiState.isSyncing, withIcon = true)
    progressMainSearchView.isEnabled = !uiState.isSyncing
    when (uiState.calendarMode) {
      CalendarMode.PRESENT_FUTURE -> progressMainCalendarIcon.setImageResource(R.drawable.ic_history)
      CalendarMode.RECENTS -> progressMainCalendarIcon.setImageResource(R.drawable.ic_calendar)
      else -> Unit
    }
  }

  private val pageChangeListener = object : ViewPager.OnPageChangeListener {
    override fun onPageSelected(position: Int) {
      if (currentPage == position) return

      progressMainCalendarIcon.fadeIf(position == 1, duration = 150)
      if (progressMainTabs.translationY != 0F) {
        resetTranslations()
        requireView().postDelayed({ onScrollReset() }, TRANSLATION_DURATION)
      }

      currentPage = position
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
    override fun onPageScrollStateChanged(state: Int) = Unit
  }
}
