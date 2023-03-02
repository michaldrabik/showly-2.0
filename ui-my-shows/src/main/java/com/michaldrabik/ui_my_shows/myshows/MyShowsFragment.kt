package com.michaldrabik.ui_my_shows.myshows

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
import com.michaldrabik.common.Config
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
import com.michaldrabik.ui_base.utilities.extensions.navigateToSafe
import com.michaldrabik.ui_base.utilities.extensions.withSpanSizeLookup
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.SortOrder.RECENTLY_WATCHED
import com.michaldrabik.ui_model.SortOrder.USER_RATING
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.R
import com.michaldrabik.ui_my_shows.main.FollowedShowsFragment
import com.michaldrabik.ui_my_shows.main.FollowedShowsUiEvent.OpenPremium
import com.michaldrabik.ui_my_shows.main.FollowedShowsViewModel
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsAdapter
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem.Type.ALL_SHOWS_HEADER
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem.Type.ALL_SHOWS_ITEM
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem.Type.RECENT_SHOWS
import com.michaldrabik.ui_navigation.java.NavigationArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_my_shows.*
import kotlinx.android.synthetic.main.fragment_watchlist.*

@AndroidEntryPoint
class MyShowsFragment :
  BaseFragment<MyShowsViewModel>(R.layout.fragment_my_shows),
  OnScrollResetListener,
  OnSearchClickListener {

  private val parentViewModel by viewModels<FollowedShowsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<MyShowsViewModel>()
  override val navigationId = R.id.followedShowsFragment

  private var adapter: MyShowsAdapter? = null
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
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = MyShowsAdapter(
      itemClickListener = { openShowDetails(it.show) },
      itemLongClickListener = { item -> openShowMenu(item.show) },
      onSortOrderClickListener = { section, order, type -> openSortOrderDialog(section, order, type) },
      onTypeClickListener = { navigateToSafe(R.id.actionFollowedShowsFragmentToMyShowsFilters) },
      onListViewModeClickListener = viewModel::toggleViewMode,
      missingImageListener = { item, force -> viewModel.loadMissingImage(item as MyShowsItem, force) },
      missingTranslationListener = { viewModel.loadMissingTranslation(it as MyShowsItem) },
      listChangeListener = {
        layoutManager?.scrollToPosition(0)
        (requireParentFragment() as FollowedShowsFragment).resetTranslations()
      }
    ).apply {
      stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }
    myShowsRecycler.apply {
      adapter = this@MyShowsFragment.adapter
      layoutManager = this@MyShowsFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
    setupRecyclerPaddings()
  }

  private fun setupRecyclerPaddings() {
    if (layoutManager is GridLayoutManager) {
      myShowsRecycler.updatePadding(
        left = dimenToPx(R.dimen.gridRecyclerPadding),
        right = dimenToPx(R.dimen.gridRecyclerPadding)
      )
    } else {
      myShowsRecycler.updatePadding(
        left = 0,
        right = 0
      )
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      myShowsRoot.updatePadding(top = statusBarHeight)
      myShowsRecycler.updatePadding(top = dimenToPx(R.dimen.myShowsTabsViewPadding))
      return
    }
    myShowsRoot.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      view.updatePadding(top = statusBarHeight)
      myShowsRecycler.updatePadding(top = dimenToPx(R.dimen.myShowsTabsViewPadding))
    }
  }

  private fun render(uiState: MyShowsUiState) {
    uiState.run {
      viewMode.let {
        if (adapter?.listViewMode != it) {
          val state = myShowsRecycler.layoutManager?.onSaveInstanceState()
          layoutManager = when (it) {
            LIST_NORMAL, LIST_COMPACT -> LinearLayoutManager(requireContext(), VERTICAL, false)
            GRID, GRID_TITLE -> GridLayoutManager(context, Config.LISTS_GRID_SPAN)
          }
          adapter?.listViewMode = it
          myShowsRecycler?.let { recycler ->
            recycler.layoutManager = layoutManager
            recycler.adapter = adapter
            recycler.layoutManager?.onRestoreInstanceState(state)
          }
          setupRecyclerPaddings()
        }
      }
      items?.let { items ->
        val notifyChangeList = resetScrollMap?.consume()
        adapter?.setItems(items, notifyChangeList)
        (layoutManager as? GridLayoutManager)?.withSpanSizeLookup { pos ->
          val item = adapter?.getItems()?.get(pos)
          when (item?.type) {
            RECENT_SHOWS, ALL_SHOWS_HEADER -> Config.LISTS_GRID_SPAN
            ALL_SHOWS_ITEM -> item.image.type.spanSize
            null -> throw Error("Unsupported span size!")
          }
        }
        myShowsEmptyView.fadeIf(showEmptyView && !isSearching)
      }
    }
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is OpenPremium -> {
        (requireParentFragment() as? FollowedShowsFragment)?.openPremium()
      }
    }
  }

  private fun openShowDetails(show: Show) {
    (requireParentFragment() as? FollowedShowsFragment)?.openShowDetails(show)
  }

  private fun openShowMenu(show: Show) {
    (requireParentFragment() as? FollowedShowsFragment)?.openShowMenu(show)
  }

  @Suppress("DEPRECATION")
  private fun openSortOrderDialog(
    section: MyShowsSection,
    order: SortOrder,
    type: SortType,
  ) {
    val options = listOf(NAME, RATING, USER_RATING, NEWEST, DATE_ADDED, RECENTLY_WATCHED)
    val key = NavigationArgs.requestSortOrderSection(section.name)
    val args = SortOrderBottomSheet.createBundle(options, order, type, key)

    requireParentFragment().setFragmentResultListener(key) { requestKey, bundle ->
      val sortOrder = bundle.getSerializable(NavigationArgs.ARG_SELECTED_SORT_ORDER) as SortOrder
      val sortType = bundle.getSerializable(NavigationArgs.ARG_SELECTED_SORT_TYPE) as SortType
      MyShowsSection.values()
        .find { NavigationArgs.requestSortOrderSection(it.name) == requestKey }
        ?.let { viewModel.setSortOrder(sortOrder, sortType) }
    }

    navigateTo(R.id.actionFollowedShowsFragmentToSortOrder, args)
  }

  override fun onEnterSearch() {
    isSearching = true
    myShowsRecycler.translationY = dimenToPx(R.dimen.myShowsSearchLocalOffset).toFloat()
    myShowsRecycler.smoothScrollToPosition(0)
  }

  override fun onExitSearch() {
    isSearching = false
    with(myShowsRecycler) {
      translationY = 0F
      postDelayed(200) { layoutManager?.scrollToPosition(0) }
    }
  }

  override fun onScrollReset() = myShowsRecycler.scrollToPosition(0)

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
