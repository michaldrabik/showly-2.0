package com.michaldrabik.ui_my_movies.main

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.GridLayout
import androidx.activity.addCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager.widget.ViewPager
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.common.views.exSearchViewIcon
import com.michaldrabik.ui_base.common.views.exSearchViewInput
import com.michaldrabik.ui_base.common.views.exSearchViewText
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.disableUi
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.enableUi
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.hideKeyboard
import com.michaldrabik.ui_base.utilities.extensions.nextPage
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showKeyboard
import com.michaldrabik.ui_base.utilities.extensions.updateTopMargin
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.mymovies.helpers.MyMoviesSearchResult
import com.michaldrabik.ui_my_movies.mymovies.helpers.ResultType.EMPTY
import com.michaldrabik.ui_my_movies.mymovies.helpers.ResultType.NO_RESULTS
import com.michaldrabik.ui_my_movies.mymovies.helpers.ResultType.RESULTS
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import com.michaldrabik.ui_my_movies.mymovies.views.MyMovieFanartView
import com.michaldrabik.ui_my_movies.utilities.OnSortClickListener
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_followed_movies.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FollowedMoviesFragment :
  BaseFragment<FollowedMoviesViewModel>(R.layout.fragment_followed_movies),
  OnTabReselectedListener,
  OnTraktSyncListener {

  override val viewModel by viewModels<FollowedMoviesViewModel>()
  private var currentPage = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    savedInstanceState?.let {
      viewModel.searchViewTranslation = it.getFloat("ARG_SEARCH_POSITION")
      viewModel.tabsTranslation = it.getFloat("ARG_TABS_POSITION")
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
          clearCache()
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    if (!followedMoviesSearchView.isSearching) {
      showNavigation()
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat("ARG_SEARCH_POSITION", followedMoviesSearchView?.translationY ?: 0F)
    outState.putFloat("ARG_TABS_POSITION", followedMoviesTabs?.translationY ?: 0F)
    outState.putInt("ARG_PAGE", followedMoviesPager?.currentItem ?: 0)
  }

  override fun onPause() {
    enableUi()
    viewModel.tabsTranslation = followedMoviesTabs.translationY
    viewModel.searchViewTranslation = followedMoviesSearchView.translationY
    super.onPause()
  }

  override fun onDestroyView() {
    followedMoviesPager.removeOnPageChangeListener(pageChangeListener)
    super.onDestroyView()
  }

  private fun setupView() {
    followedMoviesSearchView.run {
      hint = getString(R.string.textSearchFor)
      statsIconVisible = true
      onClick { enterSearch() }
      onSettingsClickListener = { openSettings() }
      onStatsClickListener = { openStatistics() }
      if (isTraktSyncing()) setTraktProgress(true)
    }
    followedMoviesModeTabs.run {
      onModeSelected = { mode = it }
      onListsSelected = { navigateTo(R.id.actionNavigateListsFragment) }
      showLists(true)
      selectMovies()
    }
    followedMoviesSortIcon.run {
      visibleIf(currentPage != 0)
      onClick {
        val currentIndex = followedMoviesPager.currentItem
        (childFragmentManager.fragments[currentIndex] as? OnSortClickListener)?.onSortClick(currentIndex)
      }
    }
    exSearchViewInput.run {
      imeOptions = EditorInfo.IME_ACTION_DONE
      setOnEditorActionListener { _, _, _ ->
        clearFocus()
        hideKeyboard()
        true
      }
    }

    followedMoviesTabs.translationY = viewModel.tabsTranslation
    followedMoviesModeTabs.translationY = viewModel.tabsTranslation
    followedMoviesSearchView.translationY = viewModel.searchViewTranslation
    followedMoviesSortIcon.translationY = viewModel.tabsTranslation
  }

  private fun setupPager() {
    followedMoviesPager.run {
      offscreenPageLimit = FollowedPagesAdapter.PAGES_COUNT
      adapter = FollowedPagesAdapter(childFragmentManager, requireAppContext())
      addOnPageChangeListener(pageChangeListener)
    }
    followedMoviesTabs.setupWithViewPager(followedMoviesPager)
  }

  private fun setupStatusBar() {
    followedMoviesRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      followedMoviesSearchView.applyWindowInsetBehaviour(dimenToPx(R.dimen.spaceNormal) + statusBarSize)
      followedMoviesSearchView.updateTopMargin(dimenToPx(R.dimen.spaceSmall) + statusBarSize)
      followedMoviesTabs.updateTopMargin(dimenToPx(R.dimen.myMoviesSearchViewPadding) + statusBarSize)
      followedMoviesModeTabs.updateTopMargin(dimenToPx(R.dimen.collectionTabsMargin) + statusBarSize)
      followedMoviesSortIcon.updateTopMargin(dimenToPx(R.dimen.myMoviesSearchViewPadding) + statusBarSize)
      followedMoviesSearchEmptyView.updateTopMargin(dimenToPx(R.dimen.searchViewHeightPadded) + statusBarSize)
      followedMoviesSearchWrapper.updateTopMargin(dimenToPx(R.dimen.searchViewHeightPadded) + statusBarSize)
    }
  }

  override fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      if (followedMoviesSearchView.isSearching) {
        exitSearch()
      } else {
        isEnabled = false
        activity?.onBackPressed()
      }
    }
  }

  private fun enterSearch() {
    if (followedMoviesSearchView.isSearching) return
    followedMoviesSearchView.isSearching = true
    exSearchViewText.gone()
    exSearchViewInput.run {
      setText("")
      doAfterTextChanged { viewModel.searchMovies(it?.toString() ?: "") }
      visible()
      showKeyboard()
      requestFocus()
    }
    (exSearchViewIcon.drawable as Animatable).start()
    exSearchViewIcon.onClick { exitSearch() }
    hideNavigation(false)
  }

  private fun exitSearch(showNavigation: Boolean = true) {
    followedMoviesSearchView.isSearching = false
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

  private fun render(uiState: FollowedMoviesUiState) {
    uiState.run {
      searchResult?.let { renderSearchResults(it) }
    }
  }

  private fun renderSearchResults(result: MyMoviesSearchResult) {
    when (result.type) {
      RESULTS -> {
        followedMoviesSearchWrapper.visible()
        followedMoviesPager.gone()
        followedMoviesTabs.gone()
        followedMoviesModeTabs.gone()
        followedMoviesSearchEmptyView.gone()
        renderSearchContainer(result.items)
      }
      NO_RESULTS -> {
        followedMoviesSearchWrapper.gone()
        followedMoviesPager.gone()
        followedMoviesTabs.gone()
        followedMoviesModeTabs.gone()
        followedMoviesSearchEmptyView.visible()
      }
      EMPTY -> {
        followedMoviesSearchWrapper.gone()
        followedMoviesPager.visible()
        followedMoviesTabs.visible()
        followedMoviesModeTabs.visible()
        followedMoviesSearchEmptyView.gone()
      }
    }

    if (result.type != EMPTY) {
      resetTranslations()
      childFragmentManager.fragments.forEach {
        (it as? OnScrollResetListener)?.onScrollReset()
      }
    }
  }

  private fun renderSearchContainer(items: List<MyMoviesItem>) {
    followedMoviesSearchContainer.removeAllViews()

    val context = requireContext()
    val itemHeight = context.dimenToPx(R.dimen.myMoviesFanartHeight)
    val itemMargin = context.dimenToPx(R.dimen.spaceTiny)

    val clickListener: (MyMoviesItem) -> Unit = {
      followedMoviesRoot.hideKeyboard()
      openMovieDetails(it.movie)
    }

    items.forEachIndexed { index, item ->
      val view = MyMovieFanartView(context).apply {
        layoutParams = FrameLayout.LayoutParams(0, MATCH_PARENT)
        bind(item, clickListener)
      }
      val layoutParams = GridLayout.LayoutParams().apply {
        width = 0
        height = itemHeight
        columnSpec = GridLayout.spec(index % 2, 1F)
        setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
      }
      followedMoviesSearchContainer.addView(view, layoutParams)
    }
  }

  fun openMovieDetails(movie: Movie) {
    disableUi()
    hideNavigation()
    followedMoviesRoot.fadeOut(150) {
      exitSearch(false)
      val bundle = Bundle().apply { putLong(ARG_MOVIE_ID, movie.ids.trakt.id) }
      navigateTo(R.id.actionFollowedMoviesFragmentToMovieDetailsFragment, bundle)
    }.add(animations)
  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionFollowedMoviesFragmentToSettingsFragment)
  }

  private fun openStatistics() {
    hideNavigation()
    navigateTo(R.id.actionFollowedMoviesFragmentToStatisticsFragment)
  }

  fun enableSearch(enable: Boolean) {
    followedMoviesSearchView.isClickable = enable
    followedMoviesSearchView.isEnabled = enable
  }

  override fun onTabReselected() {
    resetTranslations()
    followedMoviesPager.nextPage()
    childFragmentManager.fragments.forEach {
      (it as? OnScrollResetListener)?.onScrollReset()
    }
  }

  override fun onTraktSyncProgress() =
    followedMoviesSearchView.setTraktProgress(true)

  override fun onTraktSyncComplete() {
    followedMoviesSearchView.setTraktProgress(false)
    childFragmentManager.fragments.forEach {
      (it as? OnTraktSyncListener)?.onTraktSyncComplete()
    }
  }

  fun resetTranslations() {
    followedMoviesSearchView.translationY = 0F
    followedMoviesTabs.translationY = 0F
    followedMoviesModeTabs.translationY = 0F
    followedMoviesSortIcon.translationY = 0F
  }

  private val pageChangeListener = object : ViewPager.OnPageChangeListener {
    override fun onPageSelected(position: Int) {
      if (currentPage == position) return

      followedMoviesSortIcon.fadeIf(position != 0, duration = 150)
      if (followedMoviesTabs.translationY != 0F) {
        followedMoviesSearchView.animate().translationY(0F).setDuration(225L).start()
        followedMoviesTabs.animate().translationY(0F).setDuration(225L).start()
        followedMoviesModeTabs.animate().translationY(0F).setDuration(225L).start()
        followedMoviesSortIcon.animate().translationY(0F).setDuration(225L).start()
        requireView().postDelayed(
          {
            childFragmentManager.fragments.forEach {
              (it as? OnScrollResetListener)?.onScrollReset()
            }
          },
          225L
        )
      }

      currentPage = position
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
    override fun onPageScrollStateChanged(state: Int) = Unit
  }
}
