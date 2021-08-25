package com.michaldrabik.ui_my_movies.watchlist

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.main.FollowedMoviesFragment
import com.michaldrabik.ui_my_movies.utilities.OnSortClickListener
import com.michaldrabik.ui_my_movies.watchlist.recycler.WatchlistAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_watchlist_movies.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WatchlistFragment :
  BaseFragment<WatchlistViewModel>(R.layout.fragment_watchlist_movies),
  OnScrollResetListener,
  OnTraktSyncListener,
  OnSortClickListener {

  override val viewModel by viewModels<WatchlistViewModel>()

  private var adapter: WatchlistAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var statusBarHeight = 0

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupStatusBar()
    setupRecycler()

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          loadMovies()
        }
      }
    }
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
      return
    }
    watchlistMoviesContent.doOnApplyWindowInsets { view, insets, padding, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = padding.top + statusBarHeight)
    }
  }

  private fun showSortOrderDialog(order: SortOrder) {
    val options = listOf(NAME, RATING, NEWEST, DATE_ADDED)
    val optionsStrings = options.map { getString(it.displayString) }.toTypedArray()

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setTitle(R.string.textSortBy)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(optionsStrings, options.indexOf(order)) { dialog, index ->
        viewModel.setSortOrder(options[index])
        dialog.dismiss()
      }
      .show()
  }

  private fun render(uiState: WatchlistUiState) {
    uiState.run {
      items.let {
        val notifyChange = resetScroll?.consume() == true
        adapter?.setItems(it, notifyChange = notifyChange)
        watchlistMoviesEmptyView.fadeIf(it.isEmpty())
      }
      sortOrder?.let { event ->
        event.consume()?.let { showSortOrderDialog(it) }
      }
    }
  }

  private fun openMovieDetails(movie: Movie) {
    (parentFragment as? FollowedMoviesFragment)?.openMovieDetails(movie)
  }

  override fun onSortClick(page: Int) = viewModel.loadSortOrder()

  override fun onScrollReset() {
    watchlistMoviesRecycler.scrollToPosition(0)
  }

  override fun onTraktSyncComplete() = viewModel.loadMovies()

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
