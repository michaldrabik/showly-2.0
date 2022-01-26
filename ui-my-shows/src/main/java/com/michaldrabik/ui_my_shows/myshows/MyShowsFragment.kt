package com.michaldrabik.ui_my_shows.myshows

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
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
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.common.sheets.sort_order.SortOrderBottomSheet
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_model.MyShowsSection
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
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsAdapter
import com.michaldrabik.ui_navigation.java.NavigationArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_my_shows.*
import kotlinx.android.synthetic.main.fragment_watchlist.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MyShowsFragment :
  BaseFragment<MyShowsViewModel>(R.layout.fragment_my_shows),
  OnScrollResetListener,
  OnSearchClickListener,
  OnTraktSyncListener {

  private val parentViewModel by viewModels<FollowedShowsViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<MyShowsViewModel>()

  private var adapter: MyShowsAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var horizontalPositions: MutableMap<MyShowsSection, Pair<Int, Int>>? = null
  private var statusBarHeight = 0
  private var isSearching = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    savedInstanceState?.let { bundle ->
      val horizontalPositionsMap = mutableMapOf<MyShowsSection, Pair<Int, Int>>()
      MyShowsSection.values().forEach { section ->
        if (bundle.containsKey(section.name)) {
          @Suppress("UNCHECKED_CAST")
          horizontalPositionsMap[section] = bundle.getSerializable(section.name) as Pair<Int, Int>
        }
      }
      horizontalPositions = horizontalPositionsMap
    }
  }

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

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    horizontalPositions?.entries?.forEach { (section, position) ->
      outState.putSerializable(section.name, position)
    }
  }

  override fun onPause() {
    horizontalPositions = adapter?.horizontalPositions?.toMutableMap()
    super.onPause()
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = MyShowsAdapter(
      itemClickListener = { openShowDetails(it.show) },
      itemLongClickListener = { item -> openShowMenu(item.show) },
      onSortOrderClickListener = { section, order, type -> showSortOrderDialog(section, order, type) },
      missingImageListener = { item, force -> viewModel.loadMissingImage(item, force) },
      missingTranslationListener = { viewModel.loadMissingTranslation(it) },
      sectionMissingImageListener = { item, section, force -> viewModel.loadSectionMissingItem(item, section, force) },
      listChangeListener = {
        layoutManager?.scrollToPosition(0)
        (requireParentFragment() as FollowedShowsFragment).resetTranslations()
      }
    ).apply {
      stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
      horizontalPositions = this@MyShowsFragment.horizontalPositions?.toMutableMap() ?: mutableMapOf()
    }
    myShowsRecycler.apply {
      adapter = this@MyShowsFragment.adapter
      layoutManager = this@MyShowsFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
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

  private fun showSortOrderDialog(section: MyShowsSection, order: SortOrder, type: SortType) {
    val options = listOf(NAME, RATING, NEWEST, DATE_ADDED)
    val key = NavigationArgs.requestSortOrderSection(section.name)
    val args = SortOrderBottomSheet.createBundle(options, order, type, key)

    requireParentFragment().setFragmentResultListener(key) { requestKey, bundle ->
      val sortOrder = bundle.getSerializable(NavigationArgs.ARG_SELECTED_SORT_ORDER) as SortOrder
      val sortType = bundle.getSerializable(NavigationArgs.ARG_SELECTED_SORT_TYPE) as SortType
      MyShowsSection.values()
        .find { NavigationArgs.requestSortOrderSection(it.name) == requestKey }
        ?.let { viewModel.setSectionSortOrder(it, sortOrder, sortType) }
    }

    navigateTo(R.id.actionFollowedShowsFragmentToSortOrder, args)
  }

  private fun render(uiState: MyShowsUiState) {
    uiState.run {
      items?.let {
        val notifyChangeList = resetScrollMap?.consume()
        adapter?.setItems(it, notifyChangeList)
        myShowsEmptyView.fadeIf(it.isEmpty() && !isSearching)
      }
    }
  }

  private fun openShowDetails(show: Show) {
    (requireParentFragment() as? FollowedShowsFragment)?.openShowDetails(show)
  }

  private fun openShowMenu(show: Show) {
    (requireParentFragment() as? FollowedShowsFragment)?.openShowMenu(show)
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

  override fun onTraktSyncComplete() = viewModel.loadShows()

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
