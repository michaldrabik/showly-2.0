package com.michaldrabik.ui_my_movies.watchlist

import android.os.Bundle
import android.view.View
import androidx.core.view.postDelayed
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
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
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.main.FollowedMoviesFragment
import com.michaldrabik.ui_my_movies.main.FollowedMoviesViewModel
import com.michaldrabik.ui_my_movies.watchlist.recycler.WatchlistAdapter
import com.michaldrabik.ui_navigation.java.NavigationArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_watchlist_movies.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class WatchlistFragment :
  BaseFragment<WatchlistViewModel>(R.layout.fragment_watchlist_movies),
  OnScrollResetListener,
  OnTraktSyncListener,
  OnSearchClickListener,
  OnSortClickListener {

  private val parentViewModel by viewModels<FollowedMoviesViewModel>({ requireParentFragment() })
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
      doAfterLaunch = { viewModel.loadMovies() }
    )
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
    adapter = WatchlistAdapter(
      itemClickListener = { openMovieDetails(it.movie) },
      missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) },
      missingTranslationListener = { viewModel.loadMissingTranslation(it) },
      listChangeListener = {
        watchlistMoviesRecycler.scrollToPosition(0)
        (requireParentFragment() as FollowedMoviesFragment).resetTranslations()
      }
    )
    watchlistMoviesRecycler.apply {
      setHasFixedSize(true)
      adapter = this@WatchlistFragment.adapter
      layoutManager = this@WatchlistFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      watchlistMoviesContent.updatePadding(top = watchlistMoviesContent.paddingTop + statusBarHeight)
      watchlistMoviesRecycler.updatePadding(top = dimenToPx(R.dimen.watchlistMoviesTabsViewPadding))
      return
    }
    watchlistMoviesContent.doOnApplyWindowInsets { view, insets, padding, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = padding.top + statusBarHeight)
      watchlistMoviesRecycler.updatePadding(top = dimenToPx(R.dimen.watchlistMoviesTabsViewPadding))
    }
  }

  private fun showSortOrderDialog(order: SortOrder, type: SortType) {
    val options = listOf(NAME, RATING, NEWEST, DATE_ADDED)
    val args = SortOrderBottomSheet.createBundle(options, order, type)

    requireParentFragment().setFragmentResultListener(NavigationArgs.REQUEST_SORT_ORDER) { _, bundle ->
      val sortOrder = bundle.getSerializable(NavigationArgs.ARG_SELECTED_SORT_ORDER) as SortOrder
      val sortType = bundle.getSerializable(NavigationArgs.ARG_SELECTED_SORT_TYPE) as SortType
      viewModel.setSortOrder(sortOrder, sortType)
    }

    navigateTo(R.id.actionFollowedMoviesFragmentToSortOrder, args)
  }

  private fun render(uiState: WatchlistUiState) {
    uiState.run {
      items.let {
        val notifyChange = resetScroll?.consume() == true
        adapter?.setItems(it, notifyChange = notifyChange)
        watchlistMoviesEmptyView.fadeIf(it.isEmpty() && !isSearching)
      }
      sortOrder?.let { event ->
        event.consume()?.let { showSortOrderDialog(it.first, it.second) }
      }
    }
  }

  private fun openMovieDetails(movie: Movie) {
    (requireParentFragment() as? FollowedMoviesFragment)?.openMovieDetails(movie)
  }

  override fun onEnterSearch() {
    isSearching = true
    watchlistMoviesRecycler.translationY = dimenToPx(R.dimen.myMoviesSearchLocalOffset).toFloat()
    watchlistMoviesRecycler.smoothScrollToPosition(0)
  }

  override fun onExitSearch() {
    isSearching = false
    with(watchlistMoviesRecycler) {
      translationY = 0F
      postDelayed(200) { layoutManager?.scrollToPosition(0) }
    }
  }

  override fun onSortClick() = viewModel.loadSortOrder()

  override fun onScrollReset() = watchlistMoviesRecycler.scrollToPosition(0)

  override fun onTraktSyncComplete() = viewModel.loadMovies()

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
