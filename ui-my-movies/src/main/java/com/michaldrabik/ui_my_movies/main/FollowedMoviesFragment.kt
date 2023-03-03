package com.michaldrabik.ui_my_movies.main

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnSearchClickListener
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.common.sheets.context_menu.ContextMenuBottomSheet
import com.michaldrabik.ui_base.common.views.exSearchLocalViewInput
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.disableUi
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.enableUi
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.hideKeyboard
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.navigateToSafe
import com.michaldrabik.ui_base.utilities.extensions.nextPage
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showKeyboard
import com.michaldrabik.ui_base.utilities.extensions.updateTopMargin
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.PremiumFeature
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ITEM
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_followed_movies.followedMoviesIcons
import kotlinx.android.synthetic.main.fragment_followed_movies.followedMoviesModeTabs
import kotlinx.android.synthetic.main.fragment_followed_movies.followedMoviesPager
import kotlinx.android.synthetic.main.fragment_followed_movies.followedMoviesRoot
import kotlinx.android.synthetic.main.fragment_followed_movies.followedMoviesSearchIcon
import kotlinx.android.synthetic.main.fragment_followed_movies.followedMoviesSearchLocalView
import kotlinx.android.synthetic.main.fragment_followed_movies.followedMoviesSearchView
import kotlinx.android.synthetic.main.fragment_followed_movies.followedMoviesTabs

@AndroidEntryPoint
class FollowedMoviesFragment :
  BaseFragment<FollowedMoviesViewModel>(R.layout.fragment_followed_movies),
  OnTabReselectedListener {

  companion object {
    private const val TRANSLATION_DURATION = 225L
  }

  override val viewModel by viewModels<FollowedMoviesViewModel>()
  override val navigationId = R.id.followedMoviesFragment

  private var searchViewTranslation = 0F
  private var tabsViewTranslation = 0F
  private var currentPage = 0
  private var isSearching = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    savedInstanceState?.let {
      searchViewTranslation = it.getFloat("ARG_SEARCH_POSITION")
      tabsViewTranslation = it.getFloat("ARG_TABS_POSITION")
      currentPage = it.getInt("ARG_PAGE")
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupPager()
    setupStatusBar()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } }
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat("ARG_SEARCH_POSITION", followedMoviesSearchView?.translationY ?: 0F)
    outState.putFloat("ARG_TABS_POSITION", followedMoviesTabs?.translationY ?: 0F)
    outState.putInt("ARG_PAGE", followedMoviesPager?.currentItem ?: 0)
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
  }

  override fun onPause() {
    enableUi()
    tabsViewTranslation = followedMoviesTabs.translationY
    searchViewTranslation = followedMoviesSearchView.translationY
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
      onClick { openMainSearch() }
      onSettingsClickListener = { openSettings() }
      onStatsClickListener = { openStatistics() }
    }
    with(followedMoviesSearchLocalView) {
      onCloseClickListener = { exitSearch() }
    }
    followedMoviesModeTabs.run {
      onModeSelected = { mode = it }
      onListsSelected = { navigateTo(R.id.actionNavigateListsFragment) }
      showLists(true)
      selectMovies()
    }
    followedMoviesSearchIcon.run {
      onClick { if (!isSearching) enterSearch() else exitSearch() }
    }

    followedMoviesSearchView.translationY = searchViewTranslation
    followedMoviesTabs.translationY = tabsViewTranslation
    followedMoviesModeTabs.translationY = tabsViewTranslation
    followedMoviesIcons.translationY = tabsViewTranslation
  }

  private fun setupPager() {
    followedMoviesPager.run {
      offscreenPageLimit = FollowedPagesAdapter.PAGES_COUNT
      adapter = FollowedPagesAdapter(childFragmentManager, requireContext())
      addOnPageChangeListener(pageChangeListener)
    }
    followedMoviesTabs.setupWithViewPager(followedMoviesPager)
  }

  private fun setupStatusBar() {
    followedMoviesRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      followedMoviesSearchView.applyWindowInsetBehaviour(dimenToPx(R.dimen.spaceNormal) + statusBarSize)
      followedMoviesSearchView.updateTopMargin(dimenToPx(R.dimen.spaceMedium) + statusBarSize)
      followedMoviesTabs.updateTopMargin(dimenToPx(R.dimen.myMoviesSearchViewPadding) + statusBarSize)
      followedMoviesModeTabs.updateTopMargin(dimenToPx(R.dimen.collectionTabsMargin) + statusBarSize)
      followedMoviesIcons.updateTopMargin(dimenToPx(R.dimen.myMoviesSearchViewPadding) + statusBarSize)
      followedMoviesSearchLocalView.updateTopMargin(dimenToPx(R.dimen.myMoviesSearchLocalViewPadding) + statusBarSize)
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

  private fun enterSearch() {
    resetTranslations()
    followedMoviesSearchLocalView.fadeIn(150)
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
    followedMoviesSearchLocalView.gone()
    with(exSearchLocalViewInput) {
      setText("")
      gone()
      hideKeyboard()
      clearFocus()
    }
  }

  private fun openMainSearch() {
    disableUi()
    hideNavigation()
    followedMoviesModeTabs.fadeOut(duration = 200).add(animations)
    followedMoviesTabs.fadeOut(duration = 200).add(animations)
    followedMoviesIcons.fadeOut(duration = 200).add(animations)
    followedMoviesPager.fadeOut(duration = 200) {
      super.navigateTo(R.id.actionFollowedMoviesFragmentToSearch, null)
    }.add(animations)
  }

  fun openMovieDetails(movie: Movie) {
    disableUi()
    hideNavigation()
    followedMoviesRoot.fadeOut(150) {
      val bundle = Bundle().apply { putLong(ARG_MOVIE_ID, movie.traktId) }
      navigateToSafe(R.id.actionFollowedMoviesFragmentToMovieDetailsFragment, bundle)
      exitSearch()
    }.add(animations)
  }

  fun openMovieMenu(movie: Movie) {
    setFragmentResultListener(NavigationArgs.REQUEST_ITEM_MENU) { requestKey, _ ->
      if (requestKey == NavigationArgs.REQUEST_ITEM_MENU) {
        viewModel.refreshData()
      }
      clearFragmentResultListener(NavigationArgs.REQUEST_ITEM_MENU)
    }
    val bundle = ContextMenuBottomSheet.createBundle(movie.ids.trakt)
    navigateToSafe(R.id.actionFollowedMoviesFragmentToItemMenu, bundle)
  }

  fun openPremium() {
    hideNavigation()
    exitSearch()
    val args = bundleOf(ARG_ITEM to PremiumFeature.VIEW_TYPES)
    navigateToSafe(R.id.actionFollowedMoviesFragmentToPremium, args)
  }

  private fun openSettings() {
    hideNavigation()
    exitSearch()
    navigateToSafe(R.id.actionFollowedMoviesFragmentToSettingsFragment)
  }

  private fun openStatistics() {
    hideNavigation()
    exitSearch()
    navigateToSafe(R.id.actionFollowedMoviesFragmentToStatisticsFragment)
  }

  override fun onTabReselected() {
    resetTranslations(duration = 0)
    followedMoviesPager.nextPage()
    childFragmentManager.fragments.forEach {
      (it as? OnScrollResetListener)?.onScrollReset()
    }
  }

  fun resetTranslations(duration: Long = TRANSLATION_DURATION) {
    if (view == null) return
    arrayOf(
      followedMoviesSearchView,
      followedMoviesTabs,
      followedMoviesModeTabs,
      followedMoviesIcons,
      followedMoviesSearchLocalView
    ).forEach {
      it.animate().translationY(0F).setDuration(duration).add(animations)?.start()
    }
  }

  private fun render(uiState: FollowedMoviesUiState) {
    uiState.isSyncing?.let {
      followedMoviesSearchView.setTraktProgress(it)
      followedMoviesSearchView.isEnabled = !it
    }
  }

  private val pageChangeListener = object : ViewPager.OnPageChangeListener {
    override fun onPageSelected(position: Int) {
      if (currentPage == position) return

      if (followedMoviesTabs.translationY != 0F) {
        resetTranslations()
        requireView().postDelayed(
          {
            childFragmentManager.fragments.forEach { (it as? OnScrollResetListener)?.onScrollReset() }
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
