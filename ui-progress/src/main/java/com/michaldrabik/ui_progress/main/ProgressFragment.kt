package com.michaldrabik.ui_progress.main

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.updateMargins
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnShowsMoviesSyncedListener
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
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.EPISODES_LEFT
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RECENTLY_WATCHED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_EPISODE_TAB_SELECTED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_EPISODE_DETAILS
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.calendar.helpers.CalendarMode
import com.michaldrabik.ui_progress.main.adapters.ProgressAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_progress.*
import kotlinx.android.synthetic.main.fragment_progress_main.*

@AndroidEntryPoint
class ProgressFragment :
  BaseFragment<ProgressViewModel>(R.layout.fragment_progress),
  OnShowsMoviesSyncedListener,
  OnTabReselectedListener,
  OnTraktSyncListener {

  companion object {
    private const val TRANSLATION_DURATION = 225L
  }

  override val viewModel by viewModels<ProgressViewModel>()

  private var adapter: ProgressAdapter? = null

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

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      messageLiveData.observe(viewLifecycleOwner, { showSnack(it) })
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat("ARG_SEARCH_POSITION", progressSearchView?.translationY ?: 0F)
    outState.putFloat("ARG_TABS_POSITION", progressTabs?.translationY ?: 0F)
    outState.putFloat("ARG_SORT_ICON_POSITION", progressSortIcon?.translationY ?: 0F)
    outState.putFloat("ARG_RECENTS_ICON_POSITION", progressCalendarIcon?.translationY ?: 0F)
    outState.putInt("ARG_PAGE", progressPager?.currentItem ?: 0)
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
    viewModel.loadProgress()
  }

  override fun onPause() {
    tabsTranslation = progressTabs.translationY
    searchViewTranslation = progressSearchView.translationY
    sortIconTranslation = progressSortIcon.translationY
    calendarIconTranslation = progressCalendarIcon.translationY
    super.onPause()
  }

  override fun onDestroyView() {
    progressPager.removeOnPageChangeListener(pageChangeListener)
    progressPager.adapter = null
    adapter = null
    super.onDestroyView()
  }

  private fun setupView() {
    progressSortIcon.visibleIf(currentPage == 0)
    progressCalendarIcon.visibleIf(currentPage == 1)
    progressCalendarIcon.onClick {
      exitSearch()
      onScrollReset()
      resetTranslations()
      viewModel.toggleCalendarMode()
    }
    progressSearchView.run {
      hint = getString(R.string.textSearchFor)
      settingsIconVisible = true
      isClickable = false
      onClick { enterSearch() }
      onSettingsClickListener = { openSettings() }
      if (isTraktSyncing()) setTraktProgress(true)
    }
    progressPagerModeTabs.run {
      visibleIf(moviesEnabled)
      isEnabled = false
      onModeSelected = { mode = it }
      selectShows()
    }
    progressSearchView.traktIconVisible = true
    progressSearchView.onTraktClickListener = {
      navigateTo(R.id.actionProgressFragmentToTraktSyncFragment)
    }

    progressTabs.translationY = tabsTranslation
    progressPagerModeTabs.translationY = tabsTranslation
    progressSearchView.translationY = searchViewTranslation
    progressSortIcon.translationY = sortIconTranslation
    progressCalendarIcon.translationY = calendarIconTranslation
  }

  private fun setupPager() {
    adapter = ProgressAdapter(childFragmentManager, requireAppContext())
    progressPager.run {
      adapter = this@ProgressFragment.adapter
      offscreenPageLimit = ProgressAdapter.PAGES_COUNT
      addOnPageChangeListener(pageChangeListener)
    }
    progressTabs.setupWithViewPager(progressPager)
  }

  private fun setupStatusBar() {
    progressRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      val progressTabsMargin = if (moviesEnabled) R.dimen.progressSearchViewPadding else R.dimen.progressSearchViewPaddingNoModes
      (progressSearchView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceSmall))
      (progressTabs.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(progressTabsMargin))
      (progressPagerModeTabs.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.collectionTabsMargin))
      (progressSortIcon.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(progressTabsMargin))
      (progressCalendarIcon.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(progressTabsMargin))
    }
  }

  override fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      if (progressSearchView.isSearching) {
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
    progressRoot.fadeOut(150) {
      if (findNavControl()?.currentDestination?.id == R.id.progressFragment) {
        val bundle = Bundle().apply { putLong(ARG_SHOW_ID, show.traktId) }
        navigateTo(R.id.actionProgressFragmentToShowDetailsFragment, bundle)
      } else {
        showNavigation()
        progressRoot.fadeIn(50).add(animations)
      }
    }.add(animations)
  }

  fun openEpisodeDetails(show: Show, episode: Episode, season: Season) {
    setFragmentResultListener(REQUEST_EPISODE_DETAILS) { _, bundle ->
      when {
        bundle.containsKey(ACTION_EPISODE_TAB_SELECTED) -> {
          val episodeNumber = bundle.getInt(ACTION_EPISODE_TAB_SELECTED)
          val selectedEpisode = season.episodes.first { it.number == episodeNumber }
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
    }
    navigateTo(R.id.actionProgressFragmentToEpisodeDetails, bundle)
  }

  private fun openSortOrderDialog(order: SortOrder) {
    val options = listOf(NAME, NEWEST, RECENTLY_WATCHED, EPISODES_LEFT)
    val optionsStrings = options.map { getString(it.displayString) }.toTypedArray()

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setTitle(R.string.textSortBy)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(optionsStrings, options.indexOf(order)) { dialog, index ->
        viewModel.setSortOrder(options[index])
        dialog.dismiss()
      }
      .show()
  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionProgressFragmentToSettingsFragment)
  }

  private fun enterSearch() {
    if (progressSearchView.isSearching) return
    progressSearchView.isSearching = true
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
    progressSearchView.isSearching = false
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
    progressSearchView.setTraktProgress(true)

  override fun onTraktSyncComplete() {
    progressSearchView.setTraktProgress(false)
    viewModel.loadProgress()
  }

  override fun onTabReselected() {
    resetTranslations(duration = 0)
    progressPager.nextPage()
    onScrollReset()
  }

  fun resetTranslations(duration: Long = TRANSLATION_DURATION) {
    progressSearchView.animate().translationY(0F).setDuration(duration).start()
    progressTabs.animate().translationY(0F).setDuration(duration).start()
    progressPagerModeTabs.animate().translationY(0F).setDuration(duration).start()
    progressSortIcon.animate().translationY(0F).setDuration(duration).start()
    progressCalendarIcon.animate().translationY(0F).setDuration(duration).start()
  }

  private fun onScrollReset() =
    childFragmentManager.fragments.forEach { (it as? OnScrollResetListener)?.onScrollReset() }

  private fun render(uiModel: ProgressUiModel) {
    uiModel.run {
      items?.let {
        progressPagerModeTabs.isEnabled = true
        progressSearchView.isClickable = it.isNotEmpty() || !searchQuery.isNullOrBlank()
        progressSortIcon.visibleIf(it.isNotEmpty() && currentPage == 0)
        if (it.isNotEmpty() && sortOrder != null) {
          progressSortIcon.onClick { openSortOrderDialog(sortOrder) }
        }
      }
      calendarMode?.let { mode ->
        when (mode) {
          CalendarMode.PRESENT_FUTURE -> progressCalendarIcon.setImageResource(R.drawable.ic_history)
          CalendarMode.RECENTS -> progressCalendarIcon.setImageResource(R.drawable.ic_calendar)
        }
      }
    }
  }

  private val pageChangeListener = object : ViewPager.OnPageChangeListener {
    override fun onPageSelected(position: Int) {
      if (currentPage == position) return

      progressSortIcon.fadeIf(position == 0, duration = 150)
      progressCalendarIcon.fadeIf(position == 1, duration = 150)
      if (progressTabs.translationY != 0F) {
        resetTranslations()
        requireView().postDelayed({ onScrollReset() }, TRANSLATION_DURATION)
      }

      currentPage = position
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
    override fun onPageScrollStateChanged(state: Int) = Unit
  }
}
