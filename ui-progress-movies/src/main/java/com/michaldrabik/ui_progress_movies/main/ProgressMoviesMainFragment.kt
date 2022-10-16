package com.michaldrabik.ui_progress_movies.main

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
import com.michaldrabik.ui_model.CalendarMode
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_progress_movies.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_progress_main_movies.*
import timber.log.Timber

@AndroidEntryPoint
class ProgressMoviesMainFragment :
  BaseFragment<ProgressMoviesMainViewModel>(R.layout.fragment_progress_main_movies),
  OnShowsMoviesSyncedListener,
  OnTabReselectedListener {

  companion object {
    private const val TRANSLATION_DURATION = 225L
  }

  override val viewModel by viewModels<ProgressMoviesMainViewModel>()
  override val navigationId = R.id.progressMoviesMainFragment

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
      doAfterLaunch = { viewModel.loadProgress() }
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat("ARG_SEARCH_POSITION", progressMoviesSearchView?.translationY ?: 0F)
    outState.putFloat("ARG_TABS_POSITION", progressMoviesTabs?.translationY ?: 0F)
    outState.putFloat("ARG_SIDE_ICON_POSITION", progressMoviesSideIcons?.translationY ?: 0F)
    outState.putInt("ARG_PAGE", progressMoviesPager?.currentItem ?: 0)
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
  }

  override fun onPause() {
    enableUi()
    tabsTranslation = progressMoviesTabs.translationY
    searchViewTranslation = progressMoviesSearchView.translationY
    sideIconTranslation = progressMoviesSideIcons.translationY
    super.onPause()
  }

  override fun onDestroyView() {
    progressMoviesPager.removeOnPageChangeListener(pageChangeListener)
    super.onDestroyView()
  }

  private fun setupView() {
    with(progressMoviesCalendarIcon) {
      visibleIf(currentPage == 1)
      onClick { toggleCalendarMode() }
    }

    with(progressMoviesSearchIcon) {
      onClick { if (!isSearching) enterSearch() else exitSearch() }
    }

    with(progressMoviesSearchView) {
      hint = getString(R.string.textSearchFor)
      settingsIconVisible = true
      traktIconVisible = true
      isClickable = false
      onClick { openMainSearch() }
      onSettingsClickListener = { openSettings() }
      onTraktClickListener = { navigateTo(R.id.actionProgressMoviesFragmentToTraktSyncFragment) }
    }

    with(progressMoviesModeTabs) {
      visibleIf(moviesEnabled)
      onModeSelected = { mode = it }
      selectMovies()
    }

    with(progressMoviesSearchLocalView) {
      onCloseClickListener = { exitSearch() }
    }

    progressMoviesTabs.translationY = tabsTranslation
    progressMoviesModeTabs.translationY = tabsTranslation
    progressMoviesSearchView.translationY = searchViewTranslation
    progressMoviesSideIcons.translationY = sideIconTranslation
  }

  private fun setupPager() {
    progressMoviesPager.run {
      offscreenPageLimit = ProgressMoviesMainAdapter.PAGES_COUNT
      adapter = ProgressMoviesMainAdapter(childFragmentManager, requireContext())
      addOnPageChangeListener(pageChangeListener)
    }
    progressMoviesTabs.setupWithViewPager(progressMoviesPager)
  }

  private fun setupStatusBar() {
    progressMoviesRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      (progressMoviesSearchView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.spaceMedium))
      (progressMoviesModeTabs.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.collectionTabsMargin))
      (progressMoviesSearchLocalView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarSize + dimenToPx(R.dimen.progressMoviesSearchLocalViewPadding))
      arrayOf(progressMoviesSideIcons, progressMoviesTabs).forEach {
        val margin = statusBarSize + dimenToPx(R.dimen.progressMoviesSearchViewPadding)
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

  fun openMovieDetails(movie: Movie) {
    hideNavigation()
    progressMoviesRoot.fadeOut(150) {
      val bundle = Bundle().apply { putLong(ARG_MOVIE_ID, movie.ids.trakt.id) }
      navigateTo(R.id.actionProgressMoviesFragmentToMovieDetailsFragment, bundle)
      exitSearch()
    }.add(animations)
  }

  fun openMovieMenu(movie: Movie, showPinButtons: Boolean = true) {
    setFragmentResultListener(NavigationArgs.REQUEST_ITEM_MENU) { requestKey, _ ->
      if (requestKey == NavigationArgs.REQUEST_ITEM_MENU) {
        viewModel.loadProgress()
      }
      clearFragmentResultListener(NavigationArgs.REQUEST_ITEM_MENU)
    }
    val bundle = ContextMenuBottomSheet.createBundle(movie.ids.trakt, showPinButtons)
    navigateToSafe(R.id.actionProgressMoviesFragmentToItemMenu, bundle)
  }

  fun openRateDialog(movie: Movie) {
    setFragmentResultListener(NavigationArgs.REQUEST_RATING) { _, bundle ->
      when (bundle.getParcelable<Operation>(NavigationArgs.RESULT)) {
        Operation.SAVE -> showSnack(MessageEvent.Info(R.string.textRateSaved))
        Operation.REMOVE -> showSnack(MessageEvent.Info(R.string.textRateRemoved))
        else -> Timber.w("Unknown result.")
      }
      viewModel.setWatchedMovie(movie)
    }
    val bundle = RatingsBottomSheet.createBundle(movie.ids.trakt, Type.MOVIE)
    navigateTo(R.id.actionProgressMoviesFragmentToRating, bundle)
  }

  private fun openSettings() {
    hideNavigation()
    exitSearch()
    navigateTo(R.id.actionProgressMoviesFragmentToSettingsFragment)
  }

  fun openTraktSync() {
    hideNavigation()
    exitSearch()
    navigateTo(R.id.actionProgressMoviesFragmentToTraktSyncFragment)
  }

  private fun openMainSearch() {
    disableUi()
    hideNavigation()
    progressMoviesModeTabs.fadeOut(duration = 200).add(animations)
    progressMoviesTabs.fadeOut(duration = 200).add(animations)
    progressMoviesSideIcons.fadeOut(duration = 200).add(animations)
    progressMoviesPager.fadeOut(duration = 200) {
      super.navigateTo(R.id.actionProgressMoviesFragmentToSearch, null)
    }.add(animations)
  }

  private fun enterSearch() {
    progressMoviesSearchLocalView.fadeIn(150)
    resetTranslations()
    with(exSearchLocalViewInput) {
      setText("")
      doAfterTextChanged { viewModel.onSearchQuery(it?.toString() ?: "") }
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
    progressMoviesSearchLocalView.gone()
    resetTranslations()
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
    progressMoviesPager?.nextPage()
    onScrollReset()
  }

  fun resetTranslations(duration: Long = TRANSLATION_DURATION) {
    if (view == null) return
    arrayOf(
      progressMoviesSearchView,
      progressMoviesTabs,
      progressMoviesModeTabs,
      progressMoviesSideIcons,
      progressMoviesSearchLocalView
    ).forEach {
      it.animate().translationY(0F).setDuration(duration).add(animations)?.start()
    }
  }

  private fun onScrollReset() =
    childFragmentManager.fragments.forEach { (it as? OnScrollResetListener)?.onScrollReset() }

  private fun render(uiState: ProgressMoviesMainUiState) {
    progressMoviesSearchView.setTraktProgress(uiState.isSyncing, withIcon = true)
    progressMoviesSearchView.isEnabled = !uiState.isSyncing
    when (uiState.calendarMode) {
      CalendarMode.PRESENT_FUTURE -> progressMoviesCalendarIcon.setImageResource(R.drawable.ic_history)
      CalendarMode.RECENTS -> progressMoviesCalendarIcon.setImageResource(R.drawable.ic_calendar)
      else -> Unit
    }
  }

  private val pageChangeListener = object : ViewPager.OnPageChangeListener {
    override fun onPageSelected(position: Int) {
      if (currentPage == position) return

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
