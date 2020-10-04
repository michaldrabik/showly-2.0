package com.michaldrabik.ui_my_shows.main

import android.annotation.SuppressLint
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
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnTabReselectedListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.common.views.exSearchViewIcon
import com.michaldrabik.ui_base.common.views.exSearchViewInput
import com.michaldrabik.ui_base.common.views.exSearchViewText
import com.michaldrabik.ui_base.utilities.extensions.*
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.di.UiMyShowsComponentProvider
import com.michaldrabik.ui_my_shows.myshows.helpers.MyShowsSearchResult
import com.michaldrabik.ui_my_shows.myshows.helpers.ResultType.*
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import com.michaldrabik.ui_my_shows.myshows.views.MyShowFanartView
import com.michaldrabik.ui_my_shows.seelater.SeeLaterFragment
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import kotlinx.android.synthetic.main.fragment_followed_shows.*

class FollowedShowsFragment : BaseFragment<FollowedShowsViewModel>(R.layout.fragment_followed_shows),
  OnTabReselectedListener,
  OnTraktSyncListener,
  TabLayout.OnTabSelectedListener {

  override val viewModel by viewModels<FollowedShowsViewModel> { viewModelFactory }

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiMyShowsComponentProvider).provideMyShowsComponent().inject(this)
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

  private fun setupView() {
    followedShowsSearchView.run {
      hint = getString(R.string.textSearchForMyShows)
      statsIconVisible = true
      onClick { enterSearch() }
      onSettingsClickListener = { openSettings() }
      onStatsClickListener = { openStatistics() }
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
    followedShowsSearchView.translationY = viewModel.searchViewTranslation
  }

  private fun setupStatusBar() {
    followedShowsRoot.doOnApplyWindowInsets { _, insets, _, _ ->
      val statusBarSize = insets.systemWindowInsetTop
      followedShowsSearchView.applyWindowInsetBehaviour(dimenToPx(R.dimen.spaceNormal) + statusBarSize)
      followedShowsSearchView.updateTopMargin(dimenToPx(R.dimen.spaceSmall) + statusBarSize)
      followedShowsTabs.updateTopMargin(dimenToPx(R.dimen.myShowsSearchViewPadding) + statusBarSize)
      followedShowsSearchEmptyView.updateTopMargin(dimenToPx(R.dimen.searchViewHeightPadded) + statusBarSize)
      followedShowsSearchContainer.updateTopMargin(dimenToPx(R.dimen.searchViewHeightPadded) + statusBarSize)
    }
  }

  private fun setupBackPress() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      if (followedShowsSearchView.isSearching) {
        exitSearch()
      } else {
        remove()
        dispatcher.onBackPressed()
      }
    }
  }

  override fun onDestroyView() {
    followedShowsTabs.removeOnTabSelectedListener(this)
    followedShowsPager.unregisterOnPageChangeCallback(pageScrollCallback)
    seeLaterFragment = null
    super.onDestroyView()
  }

  @SuppressLint("WrongConstant")
  private fun setupPager() {
    followedShowsPager.run {
      offscreenPageLimit = FollowedPagesAdapter.PAGES_COUNT
      isUserInputEnabled = false
      adapter = FollowedPagesAdapter(this@FollowedShowsFragment)
      registerOnPageChangeCallback(pageScrollCallback)
    }

    TabLayoutMediator(followedShowsTabs, followedShowsPager) { tab, position ->
      tab.text = when (position) {
        0 -> getString(R.string.menuShows)
        1 -> getString(R.string.menuSeeLater)
        2 -> getString(R.string.menuArchive)
        else -> error("Unsupported index")
      }
    }.attach()

    followedShowsTabs.addOnTabSelectedListener(this)
  }

  override fun onTabSelected(tab: TabLayout.Tab) {
    followedShowsPager.currentItem = tab.position
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

  private fun render(uiModel: FollowedShowsUiModel) {
    uiModel.run {
      searchResult?.let { renderSearchResults(it) }
    }
  }

  private fun renderSearchResults(result: MyShowsSearchResult) {
    when (result.type) {
      RESULTS -> {
        followedShowsSearchContainer.visible()
        followedShowsPager.gone()
        followedShowsTabs.gone()
        followedShowsSearchEmptyView.gone()
        renderSearchContainer(result.items)
      }
      NO_RESULTS -> {
        followedShowsSearchContainer.gone()
        followedShowsPager.gone()
        followedShowsTabs.gone()
        followedShowsSearchEmptyView.visible()
      }
      EMPTY -> {
        followedShowsSearchContainer.gone()
        followedShowsPager.visible()
        followedShowsTabs.visible()
        followedShowsSearchEmptyView.gone()
      }
    }

    if (result.type != EMPTY) {
      followedShowsSearchView.translationY = 0F
      followedShowsTabs.translationY = 0F
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
    followedShowsRoot.fadeOut {
      enableUi()
      exitSearch(false)
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, show.ids.trakt.id) }
      navigateTo(R.id.actionFollowedShowsFragmentToShowDetailsFragment, bundle)
    }
    viewModel.tabsTranslation = followedShowsTabs.translationY
    viewModel.searchViewTranslation = followedShowsSearchView.translationY
  }

  private fun openSettings() {
    hideNavigation()
    navigateTo(R.id.actionFollowedShowsFragmentToSettingsFragment)

    viewModel.tabsTranslation = followedShowsTabs.translationY
    viewModel.searchViewTranslation = followedShowsSearchView.translationY
  }

  private fun openStatistics() {
    hideNavigation()
    navigateTo(R.id.actionFollowedShowsFragmentToStatisticsFragment)

    viewModel.tabsTranslation = followedShowsTabs.translationY
    viewModel.searchViewTranslation = followedShowsSearchView.translationY
  }

  fun enableSearch(enable: Boolean) {
    followedShowsSearchView.isClickable = enable
    followedShowsSearchView.isEnabled = enable
  }

  override fun onTabReselected() {
    followedShowsSearchView.translationY = 0F
    followedShowsTabs.translationY = 0F
    followedShowsPager.nextPage()
    childFragmentManager.fragments.forEach {
      (it as? OnScrollResetListener)?.onScrollReset()
    }
  }

  override fun onTraktSyncProgress() {
    childFragmentManager.fragments.forEach {
      (it as? OnTraktSyncListener)?.onTraktSyncProgress()
    }
  }

  private var seeLaterFragment: SeeLaterFragment? = null
  private val pageScrollCallback = object : ViewPager2.OnPageChangeCallback() {
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
      if (position == 1) {
        if (seeLaterFragment == null) {
          seeLaterFragment = (childFragmentManager.findFragmentByTag("f1") as? SeeLaterFragment)
        }
        seeLaterFragment?.onTabScrollPosition(positionOffset)
      }
    }
  }

  override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
  override fun onTabReselected(tab: TabLayout.Tab?) = Unit
}
