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
import androidx.viewpager.widget.ViewPager
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.common.OnTranslationsSyncListener
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
import com.michaldrabik.ui_my_movies.di.UiMyMoviesComponentProvider
import com.michaldrabik.ui_my_movies.mymovies.helpers.MyMoviesSearchResult
import com.michaldrabik.ui_my_movies.mymovies.helpers.ResultType.EMPTY
import com.michaldrabik.ui_my_movies.mymovies.helpers.ResultType.NO_RESULTS
import com.michaldrabik.ui_my_movies.mymovies.helpers.ResultType.RESULTS
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import com.michaldrabik.ui_my_movies.mymovies.views.MyMovieFanartView
import com.michaldrabik.ui_my_movies.utilities.OnSortClickListener
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import kotlinx.android.synthetic.main.fragment_followed_movies.*

class FollowedMoviesFragment :
  BaseFragment<FollowedMoviesViewModel>(R.layout.fragment_followed_movies),
  OnTabReselectedListener,
  OnTraktSyncListener,
  OnTranslationsSyncListener {

  override val viewModel by viewModels<FollowedMoviesViewModel> { viewModelFactory }
  private var currentPage = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiMyMoviesComponentProvider).provideMyMoviesComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupPager()
    setupStatusBar()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      clearCache()
    }
  }

  override fun onResume() {
    super.onResume()
    setupBackPress()
  }

  override fun onPause() {
    enableUi()
    super.onPause()
  }

  override fun onDestroyView() {
    followedMoviesPager.removeOnPageChangeListener(pageChangeListener)
    super.onDestroyView()
  }

  private fun setupView() {
    followedMoviesSearchView.run {
      hint = getString(R.string.textSearchForMyMovies)
      statsIconVisible = true
      onClick { enterSearch() }
      onSettingsClickListener = { openSettings() }
      onStatsClickListener = { openStatistics() }
    }
    followedMoviesModeTabs.run {
      onModeSelected = { mode = it }
      animateMovies()
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
      followedMoviesModeTabs.updateTopMargin(dimenToPx(R.dimen.showsMoviesTabsMargin) + statusBarSize)
      followedMoviesSortIcon.updateTopMargin(dimenToPx(R.dimen.myMoviesSearchViewPadding) + statusBarSize)
      followedMoviesSearchEmptyView.updateTopMargin(dimenToPx(R.dimen.searchViewHeightPadded) + statusBarSize)
      followedMoviesSearchContainer.updateTopMargin(dimenToPx(R.dimen.searchViewHeightPadded) + statusBarSize)
    }
  }

  private fun setupBackPress() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      if (followedMoviesSearchView.isSearching) {
        exitSearch()
      } else {
        remove()
        dispatcher.onBackPressed()
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

  private fun render(uiModel: FollowedMoviesUiModel) {
    uiModel.run {
      searchResult?.let { renderSearchResults(it) }
    }
  }

  private fun renderSearchResults(result: MyMoviesSearchResult) {
    when (result.type) {
      RESULTS -> {
        followedMoviesSearchContainer.visible()
        followedMoviesPager.gone()
        followedMoviesTabs.gone()
        followedMoviesModeTabs.gone()
        followedMoviesSearchEmptyView.gone()
        renderSearchContainer(result.items)
      }
      NO_RESULTS -> {
        followedMoviesSearchContainer.gone()
        followedMoviesPager.gone()
        followedMoviesTabs.gone()
        followedMoviesModeTabs.gone()
        followedMoviesSearchEmptyView.visible()
      }
      EMPTY -> {
        followedMoviesSearchContainer.gone()
        followedMoviesPager.visible()
        followedMoviesTabs.visible()
        followedMoviesModeTabs.visible()
        followedMoviesSearchEmptyView.gone()
      }
    }

    if (result.type != EMPTY) {
      followedMoviesSearchView.translationY = 0F
      followedMoviesTabs.translationY = 0F
      followedMoviesModeTabs.translationY = 0F
      followedMoviesSortIcon.translationY = 0F
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
    followedMoviesRoot.fadeOut {
      exitSearch(false)
      val bundle = Bundle().apply { putLong(ARG_MOVIE_ID, movie.ids.trakt.id) }
      navigateTo(R.id.actionFollowedMoviesFragmentToMovieDetailsFragment, bundle)
    }.add(animations)
    viewModel.tabsTranslation = followedMoviesTabs.translationY
    viewModel.searchViewTranslation = followedMoviesSearchView.translationY
  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionFollowedMoviesFragmentToSettingsFragment)

    viewModel.tabsTranslation = followedMoviesTabs.translationY
    viewModel.searchViewTranslation = followedMoviesSearchView.translationY
  }

  private fun openStatistics() {
    hideNavigation()
    navigateTo(R.id.actionFollowedMoviesFragmentToStatisticsFragment)

    viewModel.tabsTranslation = followedMoviesTabs.translationY
    viewModel.searchViewTranslation = followedMoviesSearchView.translationY
  }

  fun enableSearch(enable: Boolean) {
    followedMoviesSearchView.isClickable = enable
    followedMoviesSearchView.isEnabled = enable
  }

  override fun onTabReselected() {
    followedMoviesSearchView.translationY = 0F
    followedMoviesTabs.translationY = 0F
    followedMoviesModeTabs.translationY = 0F
    followedMoviesSortIcon.translationY = 0F
    followedMoviesPager.nextPage()
    childFragmentManager.fragments.forEach {
      (it as? OnScrollResetListener)?.onScrollReset()
    }
  }

  override fun onTraktSyncProgress() {
    childFragmentManager.fragments.forEach {
      (it as? OnTraktSyncListener)?.onTraktSyncProgress()
    }
  }

  override fun onTranslationsSyncProgress() {
    childFragmentManager.fragments.forEach {
      (it as? OnTranslationsSyncListener)?.onTranslationsSyncProgress()
    }
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
