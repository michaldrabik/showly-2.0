package com.michaldrabik.ui_progress.main

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.updateMargins
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager.widget.ViewPager
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnShowsMoviesSyncedListener
import com.michaldrabik.ui_base.common.OnSortClickListener
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.common.views.exSearchViewIcon
import com.michaldrabik.ui_base.common.views.exSearchViewInput
import com.michaldrabik.ui_base.common.views.exSearchViewText
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.hideKeyboard
import com.michaldrabik.ui_base.utilities.extensions.nextPage
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showKeyboard
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_episodes.details.EpisodeDetailsBottomSheet
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_EPISODE_TAB_SELECTED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_EPISODE_DETAILS
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.calendar.helpers.CalendarMode
import com.michaldrabik.ui_progress.main.adapters.ProgressMainAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_progress_main.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProgressMainFragment :
  BaseFragment<ProgressMainViewModel>(R.layout.fragment_progress_main),
  OnShowsMoviesSyncedListener,
  OnTabReselectedListener,
  OnTraktSyncListener {

  companion object {
    private const val TRANSLATION_DURATION = 225L
  }

  override val viewModel by viewModels<ProgressMainViewModel>()

  private var adapter: ProgressMainAdapter? = null

  private var searchViewTranslation = 0F
  private var tabsTranslation = 0F
  private var sortIconTranslation = 0F
  private var calendarIconTranslation = 0F
  private var currentPage = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    savedInstanceState?.let {
      searchViewTranslation = it.getFloat("ARG_SEARCH_POSITION")
      tabsTranslation = it.getFloat("ARG_TABS_POSITION")
      sortIconTranslation = it.getFloat("ARG_SORT_ICON_POSITION")
      calendarIconTranslation = it.getFloat("ARG_RECENTS_ICON_POSITION")
      currentPage = it.getInt("ARG_PAGE")
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupPager()
    setupStatusBar()

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          launch { messageState.collect { showSnack(it) } }
          loadProgress()
        }
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat("ARG_SEARCH_POSITION", progressMainSearchView?.translationY ?: 0F)
    outState.putFloat("ARG_TABS_POSITION", progressMainTabs?.translationY ?: 0F)
    outState.putFloat("ARG_SORT_ICON_POSITION", progressMainSortIcon?.translationY ?: 0F)
    outState.putFloat("ARG_RECENTS_ICON_POSITION", progressMainCalendarIcon?.translationY ?: 0F)
    outState.putInt("ARG_PAGE", progressMainPager?.currentItem ?: 0)
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
  }

  override fun onPause() {
    tabsTranslation = progressMainTabs.translationY
    searchViewTranslation = progressMainSearchView.translationY
    sortIconTranslation = progressMainSortIcon.translationY
    calendarIconTranslation = progressMainCalendarIcon.translationY
    super.onPause()
  }

  override fun onDestroyView() {
    progressMainPager.removeOnPageChangeListener(pageChangeListener)
    progressMainPager.adapter = null
    adapter = null
    super.onDestroyView()
  }

  private fun setupView() {
    with(progressMainSortIcon) {
      visibleIf(currentPage == 0)
      onClick { childFragmentManager.fragments.forEach { (it as? OnSortClickListener)?.onSortClick() } }
    }

    with(progressMainCalendarIcon) {
      visibleIf(currentPage == 1)
      onClick {
        exitSearch()
        onScrollReset()
        resetTranslations()
        viewModel.toggleCalendarMode()
      }
    }

    with(progressMainSearchView) {
      hint = getString(R.string.textSearchFor)
      settingsIconVisible = true
      traktIconVisible = true
      isClickable = false
      onClick { enterSearch() }
      onSettingsClickListener = { openSettings() }
      onTraktClickListener = { navigateTo(R.id.actionProgressFragmentToTraktSyncFragment) }
      if (isTraktSyncing()) setTraktProgress(true)
    }

    with(progressMainPagerModeTabs) {
      visibleIf(moviesEnabled)
      onModeSelected = { mode = it }
      selectShows()
    }

    progressMainTabs.translationY = tabsTranslation
    progressMainPagerModeTabs.translationY = tabsTranslation
    progressMainSearchView.translationY = searchViewTranslation
    progressMainSortIcon.translationY = sortIconTranslation
    progressMainCalendarIcon.translationY = calendarIconTranslation
  }

  private fun setupPager() {
    adapter = ProgressMainAdapter(childFragmentManager, requireAppContext())
    progressMainPager.run {
      adapter = this@ProgressMainFragment.adapter
      offscreenPageLimit = ProgressMainAdapter.PAGES_COUNT
      addOnPageChangeListener(pageChangeListener)
    }
    progressMainTabs.setupWithViewPager(progressMainPager)
  }

  private fun setupStatusBar() {
    progressMainRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      val progressTabsMargin = if (moviesEnabled) R.dimen.progressSearchViewPadding else R.dimen.progressSearchViewPaddingNoModes
      (progressMainSearchView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceSmall))
      (progressMainTabs.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(progressTabsMargin))
      (progressMainPagerModeTabs.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.collectionTabsMargin))
      (progressMainSortIcon.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(progressTabsMargin))
      (progressMainCalendarIcon.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(progressTabsMargin))
    }
  }

  override fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      if (progressMainSearchView.isSearching) {
        exitSearch()
      } else {
        isEnabled = false
        activity?.onBackPressed()
      }
    }
  }

  fun openTraktSync() {
    hideNavigation()
    navigateTo(R.id.actionProgressFragmentToTraktSyncFragment)
  }

  fun openShowDetails(show: Show) {
    exitSearch()
    hideNavigation()
    progressMainRoot.fadeOut(150) {
      if (findNavControl()?.currentDestination?.id == R.id.progressFragment) {
        val bundle = Bundle().apply { putLong(ARG_SHOW_ID, show.traktId) }
        navigateTo(R.id.actionProgressFragmentToShowDetailsFragment, bundle)
      } else {
        showNavigation()
        progressMainRoot.fadeIn(50).add(animations)
      }
    }.add(animations)
  }

  fun openEpisodeDetails(show: Show, episode: Episode, season: Season) {
    if (!checkNavigation(R.id.progressFragment)) return
    setFragmentResultListener(REQUEST_EPISODE_DETAILS) { _, bundle ->
      when {
        bundle.containsKey(ACTION_EPISODE_TAB_SELECTED) -> {
          val selectedEpisode = bundle.getParcelable<Episode>(ACTION_EPISODE_TAB_SELECTED)!!
          openEpisodeDetails(show, selectedEpisode, season)
        }
      }
    }
    val bundle = Bundle().apply {
      putLong(EpisodeDetailsBottomSheet.ARG_ID_TRAKT, show.traktId)
      putLong(EpisodeDetailsBottomSheet.ARG_ID_TMDB, show.ids.tmdb.id)
      putParcelable(EpisodeDetailsBottomSheet.ARG_EPISODE, episode)
      putBoolean(EpisodeDetailsBottomSheet.ARG_IS_WATCHED, false)
      putBoolean(EpisodeDetailsBottomSheet.ARG_SHOW_BUTTON, false)
      putBoolean(EpisodeDetailsBottomSheet.ARG_SHOW_TABS, true)
    }
    navigateTo(R.id.actionProgressFragmentToEpisodeDetails, bundle)
  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionProgressFragmentToSettingsFragment)
  }

  private fun enterSearch() {
    if (progressMainSearchView.isSearching) return
    progressMainSearchView.isSearching = true
    exSearchViewText.gone()
    exSearchViewInput.run {
      setText("")
      doAfterTextChanged { viewModel.onSearchQuery(it?.toString() ?: "") }
      visible()
      showKeyboard()
      requestFocus()
    }
    (exSearchViewIcon.drawable as Animatable).start()
    exSearchViewIcon.onClick { exitSearch() }
    hideNavigation(false)
  }

  private fun exitSearch() {
    progressMainSearchView.isSearching = false
    exSearchViewText.visible()
    exSearchViewInput.run {
      setText("")
      gone()
      hideKeyboard()
      clearFocus()
    }
    exSearchViewIcon.setImageResource(R.drawable.ic_anim_search_to_close)
    showNavigation()
  }

  override fun onShowsMoviesSyncFinished() = viewModel.loadProgress()

  override fun onTraktSyncProgress() =
    progressMainSearchView.setTraktProgress(true)

  override fun onTraktSyncComplete() {
    progressMainSearchView.setTraktProgress(false)
    viewModel.loadProgress()
  }

  override fun onTabReselected() {
    resetTranslations(duration = 0)
    progressMainPager.nextPage()
    onScrollReset()
  }

  fun resetTranslations(duration: Long = TRANSLATION_DURATION) {
    if (view == null) return
    progressMainSearchView.animate().translationY(0F).setDuration(duration).add(animations)?.start()
    progressMainTabs.animate().translationY(0F).setDuration(duration).add(animations)?.start()
    progressMainPagerModeTabs.animate().translationY(0F).setDuration(duration).add(animations)?.start()
    progressMainSortIcon.animate().translationY(0F).setDuration(duration).add(animations)?.start()
    progressMainCalendarIcon.animate().translationY(0F).setDuration(duration).add(animations)?.start()
  }

  private fun onScrollReset() =
    childFragmentManager.fragments.forEach { (it as? OnScrollResetListener)?.onScrollReset() }

  private fun render(uiState: ProgressMainUiState) {
    when (uiState.calendarMode) {
      CalendarMode.PRESENT_FUTURE -> progressMainCalendarIcon.setImageResource(R.drawable.ic_history)
      CalendarMode.RECENTS -> progressMainCalendarIcon.setImageResource(R.drawable.ic_calendar)
    }
  }

  private val pageChangeListener = object : ViewPager.OnPageChangeListener {
    override fun onPageSelected(position: Int) {
      if (currentPage == position) return

      progressMainSortIcon.fadeIf(position == 0, duration = 150)
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
