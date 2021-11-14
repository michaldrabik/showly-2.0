package com.michaldrabik.ui_my_shows.watchlist

import android.os.Bundle
import android.view.View
import androidx.core.view.postDelayed
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnSearchClickListener
import com.michaldrabik.ui_base.common.OnSortClickListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.common.sheets.sort_order.SortOrderBottomSheet
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.main.FollowedShowsFragment
import com.michaldrabik.ui_my_shows.main.FollowedShowsViewModel
import com.michaldrabik.ui_my_shows.watchlist.recycler.WatchlistAdapter
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SELECTED_SORT_ORDER
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SELECTED_SORT_TYPE
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_SORT_ORDER
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_my_shows.*
import kotlinx.android.synthetic.main.fragment_watchlist.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class WatchlistFragment :
  BaseFragment<WatchlistViewModel>(R.layout.fragment_watchlist),
  OnScrollResetListener,
  OnTraktSyncListener,
  OnSearchClickListener,
  OnSortClickListener {

  private val parentViewModel by viewModels<FollowedShowsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<WatchlistViewModel>()

  private var adapter: WatchlistAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var statusBarHeight = 0
  private var isSearching = false

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupStatusBar()
    setupRecycler()

    launchAndRepeatStarted(
      { parentViewModel.uiState.collect { viewModel.onParentState(it) } },
      { viewModel.uiState.collect { render(it) } },
      doAfterLaunch = { viewModel.loadShows() }
    )
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
    adapter = WatchlistAdapter().apply {
      stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
      missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
      missingTranslationListener = { viewModel.loadMissingTranslation(it) }
      itemClickListener = { openShowDetails(it.show) }
      listChangeListener = {
        watchlistRecycler.scrollToPosition(0)
        (requireParentFragment() as FollowedShowsFragment).resetTranslations()
      }
    }
    watchlistRecycler.apply {
      setHasFixedSize(true)
      adapter = this@WatchlistFragment.adapter
      layoutManager = this@WatchlistFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      watchlistContent.updatePadding(top = watchlistContent.paddingTop + statusBarHeight)
      watchlistRecycler.updatePadding(top = dimenToPx(R.dimen.watchlistTabsViewPadding))
      return
    }
    watchlistContent.doOnApplyWindowInsets { view, insets, padding, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = padding.top + statusBarHeight)
      watchlistRecycler.updatePadding(top = dimenToPx(R.dimen.watchlistTabsViewPadding))
    }
  }

  private fun showSortOrderDialog(order: SortOrder, type: SortType) {
    val options = listOf(NAME, RATING, NEWEST, DATE_ADDED)
    val args = SortOrderBottomSheet.createBundle(options, order, type)

    requireParentFragment().setFragmentResultListener(REQUEST_SORT_ORDER) { _, bundle ->
      val sortOrder = bundle.getSerializable(ARG_SELECTED_SORT_ORDER) as SortOrder
      val sortType = bundle.getSerializable(ARG_SELECTED_SORT_TYPE) as SortType
      viewModel.setSortOrder(sortOrder, sortType)
    }

    navigateTo(R.id.actionFollowedShowsFragmentToSortOrder, args)
  }

  private fun render(uiState: WatchlistUiState) {
    uiState.run {
      items.let {
        val notifyChange = resetScroll?.consume() == true
        adapter?.setItems(it, notifyChange = notifyChange)
        watchlistEmptyView.fadeIf(it.isEmpty() && !isSearching)
      }
      sortOrder?.let { event ->
        event.consume()?.let { showSortOrderDialog(it.first, it.second) }
      }
    }
  }

  private fun openShowDetails(show: Show) {
    (requireParentFragment() as? FollowedShowsFragment)?.openShowDetails(show)
  }

  override fun onEnterSearch() {
    isSearching = true
    watchlistRecycler.translationY = dimenToPx(R.dimen.myShowsSearchLocalOffset).toFloat()
    watchlistRecycler.smoothScrollToPosition(0)
  }

  override fun onExitSearch() {
    isSearching = false
    with(watchlistRecycler) {
      translationY = 0F
      postDelayed(200) { layoutManager?.scrollToPosition(0) }
    }
  }

  override fun onSortClick() = viewModel.loadSortOrder()

  override fun onScrollReset() = watchlistRecycler.scrollToPosition(0)

  override fun onTraktSyncComplete() = viewModel.loadShows()

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
