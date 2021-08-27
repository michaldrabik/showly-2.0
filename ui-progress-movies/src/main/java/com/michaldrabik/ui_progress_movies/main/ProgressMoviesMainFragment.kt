package com.michaldrabik.ui_progress_movies.main

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.updateMargins
import androidx.core.widget.doAfterTextChanged
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
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.hideKeyboard
import com.michaldrabik.ui_base.utilities.extensions.nextPage
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showKeyboard
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.calendar.helpers.CalendarMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_progress_movies.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProgressMoviesMainFragment :
  BaseFragment<ProgressMoviesMainViewModel>(R.layout.fragment_progress_movies),
  OnShowsMoviesSyncedListener,
  OnTabReselectedListener,
  OnTraktSyncListener {

  companion object {
    private const val TRANSLATION_DURATION = 225L
  }

  override val viewModel by viewModels<ProgressMoviesMainViewModel>()

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
      sortIconTranslation = it.getFloat("ARG_ICON_POSITION")
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
    outState.putFloat("ARG_SEARCH_POSITION", progressMoviesSearchView?.translationY ?: 0F)
    outState.putFloat("ARG_TABS_POSITION", progressMoviesTabs?.translationY ?: 0F)
    outState.putFloat("ARG_ICON_POSITION", progressMoviesSortIcon?.translationY ?: 0F)
    outState.putFloat("ARG_RECENTS_ICON_POSITION", progressMoviesCalendarIcon?.translationY ?: 0F)
    outState.putInt("ARG_PAGE", progressMoviesPager?.currentItem ?: 0)
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
  }

  override fun onPause() {
    tabsTranslation = progressMoviesTabs.translationY
    searchViewTranslation = progressMoviesSearchView.translationY
    sortIconTranslation = progressMoviesSortIcon.translationY
    calendarIconTranslation = progressMoviesCalendarIcon.translationY
    super.onPause()
  }

  override fun onDestroyView() {
    progressMoviesPager.removeOnPageChangeListener(pageChangeListener)
    super.onDestroyView()
  }

  private fun setupView() {
    with(progressMoviesSortIcon) {
      visibleIf(currentPage == 0)
      onClick { childFragmentManager.fragments.forEach { (it as? OnSortClickListener)?.onSortClick() } }
    }

    with(progressMoviesCalendarIcon) {
      visibleIf(currentPage == 1)
      onClick {
        exitSearch()
        onScrollReset()
        resetTranslations()
        viewModel.toggleCalendarMode()
      }
    }

    with(progressMoviesSearchView) {
      hint = getString(R.string.textSearchFor)
      settingsIconVisible = true
      traktIconVisible = true
      isClickable = false
      onClick { enterSearch() }
      onSettingsClickListener = { openSettings() }
      onTraktClickListener = { navigateTo(R.id.actionProgressMoviesFragmentToTraktSyncFragment) }
      if (isTraktSyncing()) setTraktProgress(true)
    }

    with(progressMoviesModeTabs) {
      visibleIf(moviesEnabled)
      onModeSelected = { mode = it }
      selectMovies()
    }

    progressMoviesTabs.translationY = tabsTranslation
    progressMoviesModeTabs.translationY = tabsTranslation
    progressMoviesSearchView.translationY = searchViewTranslation
    progressMoviesSortIcon.translationY = sortIconTranslation
    progressMoviesCalendarIcon.translationY = calendarIconTranslation
  }

  private fun setupPager() {
    progressMoviesPager.run {
      offscreenPageLimit = ProgressMoviesMainAdapter.PAGES_COUNT
      adapter = ProgressMoviesMainAdapter(childFragmentManager, requireAppContext())
      addOnPageChangeListener(pageChangeListener)
    }
    progressMoviesTabs.setupWithViewPager(progressMoviesPager)
  }

  private fun setupStatusBar() {
    progressMoviesRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      (progressMoviesSearchView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceSmall))
      (progressMoviesTabs.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.progressMoviesSearchViewPadding))
      (progressMoviesModeTabs.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.collectionTabsMargin))
      (progressMoviesSortIcon.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.progressMoviesSearchViewPadding))
      (progressMoviesCalendarIcon.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.progressMoviesSearchViewPadding))
    }
  }

  override fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      if (progressMoviesSearchView.isSearching) {
        exitSearch()
      } else {
        isEnabled = false
        activity?.onBackPressed()
      }
    }
  }

  fun openMovieDetails(movie: Movie) {
    exitSearch()
    hideNavigation()
    progressMoviesRoot.fadeOut(150) {
      val bundle = Bundle().apply { putLong(ARG_MOVIE_ID, movie.ids.trakt.id) }
      navigateTo(R.id.actionProgressMoviesFragmentToMovieDetailsFragment, bundle)
    }.add(animations)
  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionProgressMoviesFragmentToSettingsFragment)
  }

  fun openTraktSync() {
    hideNavigation()
    navigateTo(R.id.actionProgressMoviesFragmentToTraktSyncFragment)
  }

  private fun enterSearch() {
    if (progressMoviesSearchView.isSearching) return
    progressMoviesSearchView.isSearching = true
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

  private fun exitSearch(showNavigation: Boolean = true) {
    progressMoviesSearchView.isSearching = false
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
    progressMoviesSearchView.setTraktProgress(true)

  override fun onTraktSyncComplete() {
    progressMoviesSearchView.setTraktProgress(false)
    viewModel.loadProgress()
  }

  override fun onTabReselected() {
    resetTranslations(duration = 0)
    progressMoviesPager.nextPage()
    onScrollReset()
  }

  fun resetTranslations(duration: Long = TRANSLATION_DURATION) {
    if (view == null) return
    progressMoviesSearchView.animate().translationY(0F).setDuration(duration).add(animations)?.start()
    progressMoviesTabs.animate().translationY(0F).setDuration(duration).add(animations)?.start()
    progressMoviesModeTabs.animate().translationY(0F).setDuration(duration).add(animations)?.start()
    progressMoviesSortIcon.animate().translationY(0F).setDuration(duration).add(animations)?.start()
    progressMoviesCalendarIcon.animate().translationY(0F).setDuration(duration).add(animations)?.start()
  }

  private fun onScrollReset() =
    childFragmentManager.fragments.forEach { (it as? OnScrollResetListener)?.onScrollReset() }

  private fun render(uiState: ProgressMoviesMainUiState) {
    when (uiState.calendarMode) {
      CalendarMode.PRESENT_FUTURE -> progressMoviesCalendarIcon.setImageResource(R.drawable.ic_history)
      CalendarMode.RECENTS -> progressMoviesCalendarIcon.setImageResource(R.drawable.ic_calendar)
    }
  }

  private val pageChangeListener = object : ViewPager.OnPageChangeListener {
    override fun onPageSelected(position: Int) {
      if (currentPage == position) return

      progressMoviesSortIcon.fadeIf(position == 0, duration = 150)
      progressMoviesCalendarIcon.fadeIf(position == 1, duration = 150)
      if (progressMoviesTabs.translationY != 0F) {
        resetTranslations()
        requireView().postDelayed({ onScrollReset() }, TRANSLATION_DURATION)
      }

      currentPage = position
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
    override fun onPageScrollStateChanged(state: Int) = Unit
  }
}
