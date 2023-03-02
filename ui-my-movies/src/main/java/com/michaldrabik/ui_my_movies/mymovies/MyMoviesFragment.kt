package com.michaldrabik.ui_my_movies.mymovies

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
import com.michaldrabik.ui_base.utilities.extensions.withSpanSizeLookup
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.SortOrder.USER_RATING
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.main.FollowedMoviesFragment
import com.michaldrabik.ui_my_movies.main.FollowedMoviesUiEvent.OpenPremium
import com.michaldrabik.ui_my_movies.main.FollowedMoviesViewModel
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesAdapter
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem.Type.ALL_MOVIES_ITEM
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem.Type.HEADER
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem.Type.RECENT_MOVIES
import com.michaldrabik.ui_navigation.java.NavigationArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_my_movies.myMoviesEmptyView
import kotlinx.android.synthetic.main.fragment_my_movies.myMoviesRecycler
import kotlinx.android.synthetic.main.fragment_my_movies.myMoviesRoot

@AndroidEntryPoint
class MyMoviesFragment :
  BaseFragment<MyMoviesViewModel>(R.layout.fragment_my_movies),
  OnScrollResetListener,
  OnSearchClickListener {

  private val parentViewModel by viewModels<FollowedMoviesViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<MyMoviesViewModel>()

  private var adapter: MyMoviesAdapter? = null
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
      { viewModel.eventFlow.collect { handleEvent(it) } },
      doAfterLaunch = { viewModel.loadMovies() }
    )
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = MyMoviesAdapter(
      itemClickListener = { openMovieDetails(it.movie) },
      itemLongClickListener = { openMovieMenu(it.movie) },
      onSortOrderClickListener = { order, type -> openSortOrderDialog(order, type) },
      onListViewModeClickListener = viewModel::setNextViewMode,
      missingImageListener = { item, force -> viewModel.loadMissingImage(item, force) },
      missingTranslationListener = { viewModel.loadMissingTranslation(it) },
      listChangeListener = {
        layoutManager?.scrollToPosition(0)
        (requireParentFragment() as FollowedMoviesFragment).resetTranslations()
      }
    )
    myMoviesRecycler.apply {
      adapter = this@MyMoviesFragment.adapter
      layoutManager = this@MyMoviesFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
    setupRecyclerPaddings()
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      myMoviesRoot.updatePadding(top = statusBarHeight)
      myMoviesRecycler.updatePadding(top = dimenToPx(R.dimen.myMoviesTabsViewPadding))
      return
    }
    myMoviesRoot.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      view.updatePadding(top = statusBarHeight)
      myMoviesRecycler.updatePadding(top = dimenToPx(R.dimen.myMoviesTabsViewPadding))
    }
  }

  private fun setupRecyclerPaddings() {
    if (layoutManager is GridLayoutManager) {
      myMoviesRecycler.updatePadding(
        left = dimenToPx(R.dimen.gridRecyclerPadding),
        right = dimenToPx(R.dimen.gridRecyclerPadding)
      )
    } else {
      myMoviesRecycler.updatePadding(
        left = 0,
        right = 0
      )
    }
  }

  private fun render(uiState: MyMoviesUiState) {
    uiState.run {
      viewMode.let {
        if (adapter?.listViewMode != it) {
          val state = myMoviesRecycler.layoutManager?.onSaveInstanceState()
          layoutManager = when (it) {
            LIST_NORMAL, LIST_COMPACT -> LinearLayoutManager(requireContext(), VERTICAL, false)
            GRID, GRID_TITLE -> GridLayoutManager(context, Config.LISTS_GRID_SPAN)
          }
          adapter?.listViewMode = it
          myMoviesRecycler?.let { recycler ->
            recycler.layoutManager = layoutManager
            recycler.adapter = adapter
            recycler.layoutManager?.onRestoreInstanceState(state)
          }
          setupRecyclerPaddings()
        }
      }
      items?.let {
        val notifyChange = resetScroll?.consume() == true
        adapter?.setItems(it, notifyChange)
        (layoutManager as? GridLayoutManager)?.withSpanSizeLookup { pos ->
          val item = adapter?.getItems()?.get(pos)
          when (item?.type) {
            RECENT_MOVIES, HEADER -> Config.LISTS_GRID_SPAN
            ALL_MOVIES_ITEM -> item.image.type.spanSize
            null -> throw Error("Unsupported span size!")
          }
        }
        myMoviesEmptyView.fadeIf(showEmptyView && !isSearching)
      }
    }
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is OpenPremium -> {
        (requireParentFragment() as? FollowedMoviesFragment)?.openPremium()
      }
    }
  }

  private fun openMovieDetails(movie: Movie) {
    (requireParentFragment() as? FollowedMoviesFragment)?.openMovieDetails(movie)
  }

  private fun openMovieMenu(movie: Movie) {
    (requireParentFragment() as? FollowedMoviesFragment)?.openMovieMenu(movie)
  }

  private fun openSortOrderDialog(order: SortOrder, type: SortType) {
    val options = listOf(NAME, RATING, USER_RATING, NEWEST, DATE_ADDED)
    val args = SortOrderBottomSheet.createBundle(options, order, type)

    requireParentFragment().setFragmentResultListener(NavigationArgs.REQUEST_SORT_ORDER) { _, bundle ->
      val sortOrder = bundle.getSerializable(NavigationArgs.ARG_SELECTED_SORT_ORDER) as SortOrder
      val sortType = bundle.getSerializable(NavigationArgs.ARG_SELECTED_SORT_TYPE) as SortType
      viewModel.setSortOrder(sortOrder, sortType)
    }

    navigateTo(R.id.actionFollowedMoviesFragmentToSortOrder, args)
  }

  override fun onEnterSearch() {
    isSearching = true
    with(myMoviesRecycler) {
      translationY = dimenToPx(R.dimen.myMoviesSearchLocalOffset).toFloat()
      smoothScrollToPosition(0)
    }
  }

  override fun onExitSearch() {
    isSearching = false
    with(myMoviesRecycler) {
      translationY = 0F
      postDelayed(200) { layoutManager?.scrollToPosition(0) }
    }
  }

  override fun onScrollReset() = myMoviesRecycler.scrollToPosition(0)

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
