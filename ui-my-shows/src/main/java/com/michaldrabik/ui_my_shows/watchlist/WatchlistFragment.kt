package com.michaldrabik.ui_my_shows.watchlist

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.postDelayed
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.common.Config.LISTS_GRID_SPAN
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.ListViewMode.GRID
import com.michaldrabik.ui_base.common.ListViewMode.GRID_TITLE
import com.michaldrabik.ui_base.common.ListViewMode.LIST_COMPACT
import com.michaldrabik.ui_base.common.ListViewMode.LIST_NORMAL
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnSearchClickListener
import com.michaldrabik.ui_base.common.sheets.sort_order.SortOrderBottomSheet
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.withSpanSizeLookup
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.SortOrder.USER_RATING
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.common.recycler.CollectionAdapter
import com.michaldrabik.ui_my_shows.main.FollowedShowsFragment
import com.michaldrabik.ui_my_shows.main.FollowedShowsUiEvent.OpenPremium
import com.michaldrabik.ui_my_shows.main.FollowedShowsViewModel
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SELECTED_SORT_ORDER
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SELECTED_SORT_TYPE
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_SORT_ORDER
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_watchlist.watchlistContent
import kotlinx.android.synthetic.main.fragment_watchlist.watchlistEmptyView
import kotlinx.android.synthetic.main.fragment_watchlist.watchlistRecycler

@AndroidEntryPoint
class WatchlistFragment :
  BaseFragment<WatchlistViewModel>(R.layout.fragment_watchlist),
  OnScrollResetListener,
  OnSearchClickListener {

  private val parentViewModel by viewModels<FollowedShowsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<WatchlistViewModel>()

  private var adapter: CollectionAdapter? = null
  private var layoutManager: LayoutManager? = null
  private var statusBarHeight = 0
  private var isSearching = false

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupStatusBar()
    setupRecycler()

    launchAndRepeatStarted(
      { parentViewModel.uiState.collect { viewModel.onParentState(it) } },
      { viewModel.uiState.collect { render(it) } },
      { viewModel.eventFlow.collect { handleEvent(it) } },
      doAfterLaunch = { viewModel.loadShows() }
    )
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
    adapter = CollectionAdapter(
      itemClickListener = { openShowDetails(it.show) },
      itemLongClickListener = { item -> openShowMenu(item.show) },
      sortChipClickListener = ::openSortOrderDialog,
      upcomingChipClickListener = viewModel::setFilters,
      listViewChipClickListener = viewModel::setNextViewMode,
      missingImageListener = viewModel::loadMissingImage,
      missingTranslationListener = viewModel::loadMissingTranslation,
      listChangeListener = {
        watchlistRecycler.scrollToPosition(0)
        (requireParentFragment() as FollowedShowsFragment).resetTranslations()
      }
    ).apply {
      stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }
    watchlistRecycler.apply {
      setHasFixedSize(true)
      adapter = this@WatchlistFragment.adapter
      layoutManager = this@WatchlistFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }
    setupRecyclerPaddings()
  }

  private fun setupRecyclerPaddings() {
    if (layoutManager is GridLayoutManager) {
      watchlistRecycler.updatePadding(
        left = dimenToPx(R.dimen.gridRecyclerPadding),
        right = dimenToPx(R.dimen.gridRecyclerPadding)
      )
    } else {
      watchlistRecycler.updatePadding(
        left = 0,
        right = 0
      )
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      watchlistContent.updatePadding(top = watchlistContent.paddingTop + statusBarHeight)
      watchlistRecycler.updatePadding(top = dimenToPx(R.dimen.collectionTabsViewPadding))
      return
    }
    watchlistContent.doOnApplyWindowInsets { view, insets, padding, _ ->
      statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      view.updatePadding(top = padding.top + statusBarHeight)
      watchlistRecycler.updatePadding(top = dimenToPx(R.dimen.collectionTabsViewPadding))
    }
  }

  private fun render(uiState: WatchlistUiState) {
    uiState.run {
      viewMode.let {
        if (adapter?.listViewMode != it) {
          layoutManager = when (it) {
            LIST_NORMAL, LIST_COMPACT -> LinearLayoutManager(requireContext(), VERTICAL, false)
            GRID, GRID_TITLE -> GridLayoutManager(context, LISTS_GRID_SPAN)
          }
          adapter?.listViewMode = it
          watchlistRecycler?.let { recycler ->
            recycler.layoutManager = layoutManager
            recycler.adapter = adapter
          }
          setupRecyclerPaddings()
        }
      }
      items.let {
        val notifyChange = resetScroll?.consume() == true
        adapter?.setItems(it, notifyChange = notifyChange)
        (layoutManager as? GridLayoutManager)?.withSpanSizeLookup { pos ->
          adapter?.getItems()?.get(pos)?.image?.type?.spanSize!!
        }
        watchlistEmptyView.fadeIf(it.isEmpty() && !isSearching)
      }
      sortOrder?.let { event ->
        event.consume()?.let { openSortOrderDialog(it.first, it.second) }
      }
    }
  }

  private fun openShowDetails(show: Show) {
    (requireParentFragment() as? FollowedShowsFragment)?.openShowDetails(show)
  }

  private fun openShowMenu(show: Show) {
    (requireParentFragment() as? FollowedShowsFragment)?.openShowMenu(show)
  }

  private fun openSortOrderDialog(order: SortOrder, type: SortType) {
    val options = listOf(NAME, RATING, USER_RATING, NEWEST, DATE_ADDED)
    val args = SortOrderBottomSheet.createBundle(options, order, type)

    requireParentFragment().setFragmentResultListener(REQUEST_SORT_ORDER) { _, bundle ->
      val sortOrder = bundle.getSerializable(ARG_SELECTED_SORT_ORDER) as SortOrder
      val sortType = bundle.getSerializable(ARG_SELECTED_SORT_TYPE) as SortType
      viewModel.setSortOrder(sortOrder, sortType)
    }

    navigateTo(R.id.actionFollowedShowsFragmentToSortOrder, args)
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

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is OpenPremium -> {
        (requireParentFragment() as? FollowedShowsFragment)?.openPremium()
      }
    }
  }

  override fun onScrollReset() = watchlistRecycler.scrollToPosition(0)

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
