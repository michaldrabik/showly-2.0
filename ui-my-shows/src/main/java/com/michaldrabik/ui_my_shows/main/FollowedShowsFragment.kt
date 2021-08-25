package com.michaldrabik.ui_my_shows.main

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
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.main.utilities.OnSortClickListener
import com.michaldrabik.ui_my_shows.myshows.helpers.MyShowsSearchResult
import com.michaldrabik.ui_my_shows.myshows.helpers.ResultType.EMPTY
import com.michaldrabik.ui_my_shows.myshows.helpers.ResultType.NO_RESULTS
import com.michaldrabik.ui_my_shows.myshows.helpers.ResultType.RESULTS
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import com.michaldrabik.ui_my_shows.myshows.views.MyShowFanartView
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_followed_shows.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FollowedShowsFragment :
  BaseFragment<FollowedShowsViewModel>(R.layout.fragment_followed_shows),
  OnTabReselectedListener,
  OnTraktSyncListener {

  override val viewModel by viewModels<FollowedShowsViewModel>()
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
    if (!followedShowsSearchView.isSearching) {
      showNavigation()
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat("ARG_SEARCH_POSITION", followedShowsSearchView?.translationY ?: 0F)
    outState.putFloat("ARG_TABS_POSITION", followedShowsTabs?.translationY ?: 0F)
    outState.putInt("ARG_PAGE", followedShowsPager?.currentItem ?: 0)
  }

  override fun onPause() {
    enableUi()
    viewModel.tabsTranslation = followedShowsTabs.translationY
    viewModel.searchViewTranslation = followedShowsSearchView.translationY
    super.onPause()
  }

  override fun onDestroyView() {
    followedShowsPager.removeOnPageChangeListener(pageChangeListener)
    super.onDestroyView()
  }

  private fun setupView() {
    followedShowsSearchView.run {
      hint = getString(R.string.textSearchFor)
      statsIconVisible = true
      onClick { enterSearch() }
      onSettingsClickListener = { openSettings() }
      onStatsClickListener = { openStatistics() }
      if (isTraktSyncing()) setTraktProgress(true)
    }
    followedShowsModeTabs.run {
      onModeSelected = { mode = it }
      onListsSelected = { navigateTo(R.id.actionNavigateListsFragment) }
      showMovies(moviesEnabled)
      showLists(true, anchorEnd = moviesEnabled)
      selectShows()
    }
    followedShowsSortIcon.run {
      visibleIf(currentPage != 0)
      onClick {
        val currentIndex = followedShowsPager.currentItem
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

    followedShowsTabs.translationY = viewModel.tabsTranslation
    followedShowsModeTabs.translationY = viewModel.tabsTranslation
    followedShowsSearchView.translationY = viewModel.searchViewTranslation
    followedShowsSortIcon.translationY = viewModel.tabsTranslation
  }

  private fun setupPager() {
    followedShowsPager.run {
      offscreenPageLimit = FollowedPagesAdapter.PAGES_COUNT
      adapter = FollowedPagesAdapter(childFragmentManager, requireAppContext())
      addOnPageChangeListener(pageChangeListener)
    }
    followedShowsTabs.setupWithViewPager(followedShowsPager)
  }

  private fun setupStatusBar() {
    followedShowsRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      followedShowsSearchView.applyWindowInsetBehaviour(dimenToPx(R.dimen.spaceNormal) + statusBarSize)
      followedShowsSearchView.updateTopMargin(dimenToPx(R.dimen.spaceSmall) + statusBarSize)
      followedShowsModeTabs.updateTopMargin(dimenToPx(R.dimen.collectionTabsMargin) + statusBarSize)
      followedShowsTabs.updateTopMargin(dimenToPx(R.dimen.myShowsSearchViewPadding) + statusBarSize)
      followedShowsSortIcon.updateTopMargin(dimenToPx(R.dimen.myShowsSearchViewPadding) + statusBarSize)
      followedShowsSearchEmptyView.updateTopMargin(dimenToPx(R.dimen.searchViewHeightPadded) + statusBarSize)
      followedShowsSearchWrapper.updateTopMargin(dimenToPx(R.dimen.searchViewHeightPadded) + statusBarSize)
    }
  }

  override fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      if (followedShowsSearchView.isSearching) {
        exitSearch()
      } else {
        isEnabled = false
        activity?.onBackPressed()
      }
    }
  }

  private fun enterSearch() {
    if (followedShowsSearchView.isSearching) return
    followedShowsSearchView.isSearching = true
    exSearchViewText.gone()
    exSearchViewInput.run {
      setText("")
      doAfterTextChanged { viewModel.searchFollowedShows(it?.toString() ?: "") }
      visible()
      showKeyboard()
      requestFocus()
    }
    (exSearchViewIcon.drawable as Animatable).start()
    exSearchViewIcon.onClick { exitSearch() }
    hideNavigation(false)
  }

  private fun exitSearch(showNavigation: Boolean = true) {
    followedShowsSearchView.isSearching = false
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

  private fun render(uiState: FollowedShowsUiState) {
    uiState.run {
      searchResult?.let { renderSearchResults(it) }
    }
  }

  private fun renderSearchResults(result: MyShowsSearchResult) {
    when (result.type) {
      RESULTS -> {
        followedShowsSearchWrapper.visible()
        followedShowsPager.gone()
        followedShowsTabs.gone()
        followedShowsModeTabs.gone()
        followedShowsSearchEmptyView.gone()
        renderSearchContainer(result.items)
      }
      NO_RESULTS -> {
        followedShowsSearchWrapper.gone()
        followedShowsPager.gone()
        followedShowsTabs.gone()
        followedShowsModeTabs.gone()
        followedShowsSearchEmptyView.visible()
      }
      EMPTY -> {
        followedShowsSearchWrapper.gone()
        followedShowsPager.visible()
        followedShowsTabs.visible()
        followedShowsModeTabs.visible()
        followedShowsSearchEmptyView.gone()
      }
    }

    if (result.type != EMPTY) {
      resetTranslations()
      childFragmentManager.fragments.forEach {
        (it as? OnScrollResetListener)?.onScrollReset()
      }
    }
  }

  private fun renderSearchContainer(items: List<MyShowsItem>) {
    followedShowsSearchContainer.removeAllViews()

    val context = requireContext()
    val itemHeight = context.dimenToPx(R.dimen.myShowsFanartHeight)
    val itemMargin = context.dimenToPx(R.dimen.spaceTiny)

    val clickListener: (MyShowsItem) -> Unit = {
      followedShowsRoot.hideKeyboard()
      openShowDetails(it.show)
    }

    items.forEachIndexed { index, item ->
      val view = MyShowFanartView(context).apply {
        layoutParams = FrameLayout.LayoutParams(0, MATCH_PARENT)
        bind(item, clickListener)
      }
      val layoutParams = GridLayout.LayoutParams().apply {
        width = 0
        height = itemHeight
        columnSpec = GridLayout.spec(index % 2, 1F)
        setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
      }
      followedShowsSearchContainer.addView(view, layoutParams)
    }
  }

  fun openShowDetails(show: Show) {
    disableUi()
    hideNavigation()
    followedShowsRoot.fadeOut(150) {
      exitSearch(false)
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, show.traktId) }
      navigateTo(R.id.actionFollowedShowsFragmentToShowDetailsFragment, bundle)
    }.add(animations)
  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionFollowedShowsFragmentToSettingsFragment)
  }

  private fun openStatistics() {
    hideNavigation()
    navigateTo(R.id.actionFollowedShowsFragmentToStatisticsFragment)
  }

  fun enableSearch(enable: Boolean) {
    followedShowsSearchView.isClickable = enable
    followedShowsSearchView.isEnabled = enable
  }

  override fun onTabReselected() {
    resetTranslations()
    followedShowsPager.nextPage()
    childFragmentManager.fragments.forEach {
      (it as? OnScrollResetListener)?.onScrollReset()
    }
  }

  fun resetTranslations() {
    followedShowsSearchView.translationY = 0F
    followedShowsTabs.translationY = 0F
    followedShowsModeTabs.translationY = 0F
    followedShowsSortIcon.translationY = 0F
  }

  override fun onTraktSyncProgress() =
    followedShowsSearchView.setTraktProgress(true)

  override fun onTraktSyncComplete() {
    followedShowsSearchView.setTraktProgress(false)
    childFragmentManager.fragments.forEach {
      (it as? OnTraktSyncListener)?.onTraktSyncComplete()
    }
  }

  private val pageChangeListener = object : ViewPager.OnPageChangeListener {
    override fun onPageSelected(position: Int) {
      if (currentPage == position) return

      followedShowsSortIcon.fadeIf(position != 0, duration = 150)
      if (followedShowsTabs.translationY != 0F) {
        followedShowsSearchView.animate().translationY(0F).setDuration(225L).start()
        followedShowsTabs.animate().translationY(0F).setDuration(225L).start()
        followedShowsModeTabs.animate().translationY(0F).setDuration(225L).start()
        followedShowsSortIcon.animate().translationY(0F).setDuration(225L).start()
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
