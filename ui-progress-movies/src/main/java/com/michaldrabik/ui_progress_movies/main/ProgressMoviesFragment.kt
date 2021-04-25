package com.michaldrabik.ui_progress_movies.main

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.updateMargins
import androidx.core.widget.doAfterTextChanged
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
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.hideKeyboard
import com.michaldrabik.ui_base.utilities.extensions.nextPage
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showKeyboard
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.di.UiProgressMoviesComponentProvider
import kotlinx.android.synthetic.main.fragment_progress_movies.*

class ProgressMoviesFragment :
  BaseFragment<ProgressMoviesViewModel>(R.layout.fragment_progress_movies),
  OnShowsMoviesSyncedListener,
  OnTabReselectedListener,
  OnTraktSyncListener {

  override val viewModel by viewModels<ProgressMoviesViewModel> { viewModelFactory }

  private var searchViewTranslation = 0F
  private var tabsTranslation = 0F
  private var sortIconTranslation = 0F
  private var currentPage = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireAppContext() as UiProgressMoviesComponentProvider).provideProgressMoviesComponent().inject(this)
    super.onCreate(savedInstanceState)
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
    outState.putFloat("ARG_SEARCH_POSITION", progressMoviesSearchView?.translationY ?: 0F)
    outState.putFloat("ARG_TABS_POSITION", progressMoviesTabs?.translationY ?: 0F)
    outState.putFloat("ARG_ICON_POSITION", progressMoviesSortIcon?.translationY ?: 0F)
    outState.putInt("ARG_PAGE", progressMoviesPager?.currentItem ?: 0)
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
    viewModel.loadProgress()
  }

  override fun onPause() {
    tabsTranslation = progressMoviesTabs.translationY
    searchViewTranslation = progressMoviesSearchView.translationY
    sortIconTranslation = progressMoviesSortIcon.translationY
    super.onPause()
  }

  override fun onDestroyView() {
    progressMoviesPager.removeOnPageChangeListener(pageChangeListener)
    super.onDestroyView()
  }

  private fun setupView() {
    progressMoviesSortIcon.visibleIf(currentPage == 0)
    progressMoviesSearchView.run {
      hint = getString(R.string.textSearchFor)
      settingsIconVisible = true
      isClickable = false
      onClick { enterSearch() }
      onSettingsClickListener = { openSettings() }
      if (isTraktSyncing()) setTraktProgress(true)
    }
    progressMoviesModeTabs.run {
      visibleIf(moviesEnabled)
      isEnabled = false
      onModeSelected = { mode = it }
      selectMovies()
    }
    progressMoviesSearchView.traktIconVisible = true
    progressMoviesSearchView.onTraktClickListener = {
      navigateTo(R.id.actionProgressMoviesFragmentToTraktSyncFragment)
    }

    progressMoviesTabs.translationY = tabsTranslation
    progressMoviesModeTabs.translationY = tabsTranslation
    progressMoviesSearchView.translationY = searchViewTranslation
    progressMoviesSortIcon.translationY = sortIconTranslation
  }

  private fun setupPager() {
    progressMoviesPager.run {
      offscreenPageLimit = ProgressMoviesPagesAdapter.PAGES_COUNT
      adapter = ProgressMoviesPagesAdapter(childFragmentManager, requireAppContext())
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

  fun openMovieDetails(item: ProgressMovieItem) {
    exitSearch()
    hideNavigation()
    progressMoviesRoot.fadeOut(150) {
      val bundle = Bundle().apply { putLong(ARG_MOVIE_ID, item.movie.ids.trakt.id) }
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

  private fun openSortOrderDialog(order: SortOrder) {
    val options = listOf(NAME, RATING, NEWEST, DATE_ADDED)
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
    if (progressMoviesSearchView.isSearching) return
    progressMoviesSearchView.isSearching = true
    exSearchViewText.gone()
    exSearchViewInput.run {
      setText("")
      doAfterTextChanged { viewModel.searchQuery(it?.toString() ?: "") }
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
    progressMoviesSearchView.translationY = 0F
    progressMoviesTabs.translationY = 0F
    progressMoviesModeTabs.translationY = 0F
    progressMoviesSortIcon.translationY = 0F
    progressMoviesPager.nextPage()
    childFragmentManager.fragments.forEach {
      (it as? OnScrollResetListener)?.onScrollReset()
    }
  }

  fun resetTranslations() {
    progressMoviesSearchView.animate().translationY(0F).start()
    progressMoviesTabs.animate().translationY(0F).start()
    progressMoviesModeTabs.animate().translationY(0F).start()
    progressMoviesSortIcon.animate().translationY(0F).start()
  }

  private fun render(uiModel: ProgressMoviesUiModel) {
    uiModel.run {
      items?.let {
        progressMoviesModeTabs.isEnabled = true
        progressMoviesSearchView.isClickable = it.isNotEmpty() || isSearching == true
        progressMoviesSortIcon.visibleIf(it.isNotEmpty() && currentPage == 0)
        if (it.isNotEmpty() && sortOrder != null) {
          progressMoviesSortIcon.onClick { openSortOrderDialog(sortOrder) }
        }
      }
    }
  }

  private val pageChangeListener = object : ViewPager.OnPageChangeListener {
    override fun onPageSelected(position: Int) {
      if (currentPage == position) return

      progressMoviesSortIcon.fadeIf(position == 0, duration = 150)
      if (progressMoviesTabs.translationY != 0F) {
        val duration = 225L
        progressMoviesSearchView.animate().translationY(0F).setDuration(duration).start()
        progressMoviesTabs.animate().translationY(0F).setDuration(duration).start()
        progressMoviesModeTabs.animate().translationY(0F).setDuration(duration).start()
        progressMoviesSortIcon.animate().translationY(0F).setDuration(duration).start()
        requireView().postDelayed(
          {
            childFragmentManager.fragments.forEach {
              (it as? OnScrollResetListener)?.onScrollReset()
            }
          },
          duration
        )
      }

      currentPage = position
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
    override fun onPageScrollStateChanged(state: Int) = Unit
  }
}
