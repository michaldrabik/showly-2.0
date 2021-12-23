package com.michaldrabik.ui_my_shows.main

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnSearchClickListener
import com.michaldrabik.ui_base.common.OnSortClickListener
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.common.sheets.context_menu.ContextMenuBottomSheet
import com.michaldrabik.ui_base.common.views.exSearchLocalViewInput
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
import com.michaldrabik.ui_base.utilities.extensions.nextPage
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showKeyboard
import com.michaldrabik.ui_base.utilities.extensions.updateTopMargin
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_followed_shows.*

@AndroidEntryPoint
class FollowedShowsFragment :
  BaseFragment<FollowedShowsViewModel>(R.layout.fragment_followed_shows),
  OnTabReselectedListener,
  OnTraktSyncListener {

  companion object {
    private const val TRANSLATION_DURATION = 225L
  }

  override val viewModel by viewModels<FollowedShowsViewModel>()

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
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putFloat("ARG_SEARCH_POSITION", followedShowsSearchView?.translationY ?: 0F)
    outState.putFloat("ARG_TABS_POSITION", followedShowsTabs?.translationY ?: 0F)
    outState.putInt("ARG_PAGE", followedShowsPager?.currentItem ?: 0)
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
  }

  override fun onPause() {
    enableUi()
    tabsViewTranslation = followedShowsTabs.translationY
    searchViewTranslation = followedShowsSearchView.translationY
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
      onClick { openMainSearch() }
      onSettingsClickListener = { openSettings() }
      onStatsClickListener = { openStatistics() }
      if (isTraktSyncing()) setTraktProgress(true)
    }
    with(followedShowsSearchLocalView) {
      onCloseClickListener = { exitSearch() }
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
        (childFragmentManager.fragments[currentIndex] as? OnSortClickListener)?.onSortClick()
      }
    }
    followedShowsSearchIcon.run {
      onClick { if (!isSearching) enterSearch() else exitSearch() }
    }

    followedShowsSearchView.translationY = searchViewTranslation
    followedShowsTabs.translationY = tabsViewTranslation
    followedShowsModeTabs.translationY = tabsViewTranslation
    followedShowsIcons.translationY = tabsViewTranslation
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
      followedShowsIcons.updateTopMargin(dimenToPx(R.dimen.myShowsSearchViewPadding) + statusBarSize)
      followedShowsSearchLocalView.updateTopMargin(dimenToPx(R.dimen.myShowsSearchLocalViewPadding) + statusBarSize)
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
    followedShowsSearchLocalView.fadeIn(150)
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
    followedShowsSearchLocalView.gone()
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
    followedShowsModeTabs.fadeOut(duration = 200).add(animations)
    followedShowsTabs.fadeOut(duration = 200).add(animations)
    followedShowsIcons.fadeOut(duration = 200).add(animations)
    followedShowsPager.fadeOut(duration = 200) {
      super.navigateTo(R.id.actionFollowedShowsFragmentToSearch, null)
    }.add(animations)
  }

  fun openShowDetails(show: Show) {
    disableUi()
    hideNavigation()
    followedShowsRoot.fadeOut(150) {
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, show.traktId) }
      navigateTo(R.id.actionFollowedShowsFragmentToShowDetailsFragment, bundle)
      exitSearch()
    }.add(animations)
  }

  fun openShowMenu(show: Show) {
    val bundle = ContextMenuBottomSheet.createBundle(show.ids.trakt)
    navigateTo(R.id.actionFollowedShowsFragmentToItemMenu, bundle)
  }

  private fun openSettings() {
    hideNavigation()
    exitSearch()
    navigateTo(R.id.actionFollowedShowsFragmentToSettingsFragment)
  }

  private fun openStatistics() {
    hideNavigation()
    exitSearch()
    navigateTo(R.id.actionFollowedShowsFragmentToStatisticsFragment)
  }

  override fun onTabReselected() {
    resetTranslations(duration = 0)
    followedShowsPager.nextPage()
    childFragmentManager.fragments.forEach {
      (it as? OnScrollResetListener)?.onScrollReset()
    }
  }

  fun resetTranslations(duration: Long = TRANSLATION_DURATION) {
    if (view == null) return
    arrayOf(
      followedShowsSearchView,
      followedShowsTabs,
      followedShowsModeTabs,
      followedShowsIcons,
      followedShowsSearchLocalView
    ).forEach {
      it.animate().translationY(0F).setDuration(duration).add(animations)?.start()
    }
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
