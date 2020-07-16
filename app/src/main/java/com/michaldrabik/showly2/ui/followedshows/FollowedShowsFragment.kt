package com.michaldrabik.showly2.ui.followedshows

import android.annotation.SuppressLint
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.GridLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.common.OnScrollResetListener
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.OnTraktSyncListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.MyShowsSearchResult
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.ResultType.EMPTY
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.ResultType.NO_RESULTS
import com.michaldrabik.showly2.ui.followedshows.myshows.helpers.ResultType.RESULTS
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsItem
import com.michaldrabik.showly2.ui.followedshows.myshows.views.MyShowFanartView
import com.michaldrabik.showly2.ui.followedshows.seelater.SeeLaterFragment
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.disableUi
import com.michaldrabik.showly2.utilities.extensions.enableUi
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.hideKeyboard
import com.michaldrabik.showly2.utilities.extensions.nextPage
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.showKeyboard
import com.michaldrabik.showly2.utilities.extensions.visible
import kotlinx.android.synthetic.main.fragment_followed_shows.*
import kotlinx.android.synthetic.main.view_search.*

class FollowedShowsFragment : BaseFragment<FollowedShowsViewModel>(R.layout.fragment_followed_shows),
  OnTabReselectedListener,
  OnTraktSyncListener {

  override val viewModel by viewModels<FollowedShowsViewModel> { viewModelFactory }

  override fun onCreate(savedInstanceState: Bundle?) {
    fragmentComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupPager()
    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
      clearCache()
    }
  }

  private fun setupView() {
    followedShowsSearchView.run {
      hint = getString(R.string.textSearchForMyShows)
      onClick { enterSearch() }
      onSettingsClickListener = { openSettings() }
    }

    searchViewInput.run {
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

  override fun onDestroyView() {
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

    followedShowsTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
      override fun onTabSelected(tab: TabLayout.Tab) {
        followedShowsPager.currentItem = tab.position
      }

      override fun onTabReselected(tab: TabLayout.Tab) = Unit
      override fun onTabUnselected(tab: TabLayout.Tab) = Unit
    })
    TabLayoutMediator(followedShowsTabs, followedShowsPager) { tab, position ->
      tab.text = when (position) {
        0 -> getString(R.string.tabMyShows)
        1 -> getString(R.string.tabSeeLater)
        else -> getString(R.string.tabStatistics)
      }
    }.attach()
  }

  private fun enterSearch() {
    if (followedShowsSearchView.isSearching) return
    followedShowsSearchView.isSearching = true
    searchViewText.gone()
    searchViewInput.run {
      setText("")
      doAfterTextChanged { viewModel.searchFollowedShows(it?.toString() ?: "") }
      visible()
      showKeyboard()
      requestFocus()
    }
    (searchViewIcon.drawable as Animatable).start()
    searchViewIcon.onClick { exitSearch() }
    hideNavigation(false)
  }

  private fun exitSearch(showNavigation: Boolean = true) {
    followedShowsSearchView.isSearching = false
    searchViewText.visible()
    searchViewInput.run {
      setText("")
      gone()
      hideKeyboard()
      clearFocus()
    }
    searchViewIcon.setImageResource(R.drawable.ic_anim_search_to_close)
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
      (it as? OnTabReselectedListener)?.onTabReselected()
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
}
