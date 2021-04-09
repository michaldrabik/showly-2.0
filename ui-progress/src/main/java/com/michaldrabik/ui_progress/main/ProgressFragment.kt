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
import com.michaldrabik.ui_progress.ProgressItem
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.di.UiProgressComponentProvider
import kotlinx.android.synthetic.main.fragment_progress.*

class ProgressFragment :
  BaseFragment<ProgressViewModel>(R.layout.fragment_progress),
  OnShowsMoviesSyncedListener,
  OnTabReselectedListener,
  OnTraktSyncListener {

  override val viewModel by viewModels<ProgressViewModel> { viewModelFactory }

  private var searchViewTranslation = 0F
  private var tabsTranslation = 0F
  private var sortIconTranslation = 0F
  private var currentPage = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiProgressComponentProvider).provideProgressComponent().inject(this)
    super.onCreate(savedInstanceState)
    setupBackPressed()
    savedInstanceState?.let {
      searchViewTranslation = it.getFloat("ARG_SEARCH_POSITION")
      tabsTranslation = it.getFloat("ARG_TABS_POSITION")
      sortIconTranslation = it.getFloat("ARG_ICON_POSITION")
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
    outState.putFloat("ARG_ICON_POSITION", progressSortIcon?.translationY ?: 0F)
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
    super.onPause()
  }

  override fun onDestroyView() {
    progressPager.removeOnPageChangeListener(pageChangeListener)
    super.onDestroyView()
  }

  private fun setupView() {
    progressSortIcon.visibleIf(currentPage == 0)
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
  }

  private fun setupPager() {
    progressPager.run {
      offscreenPageLimit = ProgressPagesAdapter.PAGES_COUNT
      adapter = ProgressPagesAdapter(childFragmentManager, requireAppContext())
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
    }
  }

  private fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(this) {
      if (progressSearchView.isSearching) {
        exitSearch()
      } else {
        remove()
        dispatcher.onBackPressed()
      }
    }
  }

  fun openShowDetails(item: ProgressItem) {
    exitSearch()
    hideNavigation()
    progressRoot.fadeOut(150) {
      if (findNavControl()?.currentDestination?.id == R.id.progressFragment) {
        val bundle = Bundle().apply { putLong(ARG_SHOW_ID, item.show.traktId) }
        navigateTo(R.id.actionProgressFragmentToShowDetailsFragment, bundle)
      } else {
        showNavigation()
        progressRoot.fadeIn(50).add(animations)
      }
    }.add(animations)
  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionProgressFragmentToSettingsFragment)
  }

  fun openTraktSync() {
    navigateTo(R.id.actionProgressFragmentToTraktSyncFragment)
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

      val seasonEpisodes = season.episodes.map { it.number }.toIntArray()
      putIntArray(EpisodeDetailsBottomSheet.ARG_SEASON_EPISODES, seasonEpisodes)

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

  private fun enterSearch() {
    if (progressSearchView.isSearching) return
    progressSearchView.isSearching = true
    exSearchViewText.gone()
    exSearchViewInput.run {
      setText("")
      doAfterTextChanged { viewModel.searchWatchlist(it?.toString() ?: "") }
      visible()
      showKeyboard()
      requestFocus()
    }
    (exSearchViewIcon.drawable as Animatable).start()
    exSearchViewIcon.onClick { exitSearch() }
    hideNavigation(false)
  }

  private fun exitSearch(showNavigation: Boolean = true) {
    progressSearchView.isSearching = false
    exSearchViewText.visible()
    exSearchViewInput.run {
      setText("")
      gone()
      hideKeyboard()
      clearFocus()
    }
    exSearchViewIcon.setImageResource(R.drawable.ic_anim_search_to_close)
    if (showNavigation) showNavigation()
  }

  override fun onShowsMoviesSyncFinished() = viewModel.loadProgress()

  override fun onTraktSyncProgress() =
    progressSearchView.setTraktProgress(true)

  override fun onTraktSyncComplete() {
    progressSearchView.setTraktProgress(false)
    viewModel.loadProgress()
  }

  override fun onTabReselected() {
    progressSearchView.translationY = 0F
    progressTabs.translationY = 0F
    progressPagerModeTabs.translationY = 0F
    progressSortIcon.translationY = 0F
    progressPager.nextPage()
    childFragmentManager.fragments.forEach {
      (it as? OnScrollResetListener)?.onScrollReset()
    }
  }

  fun resetTranslations() {
    progressSearchView.animate().translationY(0F).start()
    progressTabs.animate().translationY(0F).start()
    progressPagerModeTabs.animate().translationY(0F).start()
    progressSortIcon.animate().translationY(0F).start()
  }

  private fun render(uiModel: ProgressUiModel) {
    uiModel.run {
      items?.let {
        progressPagerModeTabs.isEnabled = true
        progressSearchView.isClickable = it.isNotEmpty() || isSearching == true
        progressSortIcon.visibleIf(it.isNotEmpty() && currentPage == 0)
        if (it.isNotEmpty() && sortOrder != null) {
          progressSortIcon.onClick { openSortOrderDialog(sortOrder) }
        }
      }
    }
  }

  private val pageChangeListener = object : ViewPager.OnPageChangeListener {
    override fun onPageSelected(position: Int) {
      if (currentPage == position) return

      progressSortIcon.fadeIf(position == 0, duration = 150)
      if (progressTabs.translationY != 0F) {
        val duration = 225L
        progressSearchView.animate().translationY(0F).setDuration(duration).start()
        progressTabs.animate().translationY(0F).setDuration(duration).start()
        progressPagerModeTabs.animate().translationY(0F).setDuration(duration).start()
        progressSortIcon.animate().translationY(0F).setDuration(duration).start()
        requireView().postDelayed({
          childFragmentManager.fragments.forEach { (it as? OnScrollResetListener)?.onScrollReset() }
        }, duration)
      }

      currentPage = position
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
    override fun onPageScrollStateChanged(state: Int) = Unit
  }
}
