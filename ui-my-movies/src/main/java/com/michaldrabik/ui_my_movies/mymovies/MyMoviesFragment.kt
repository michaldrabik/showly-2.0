package com.michaldrabik.ui_my_movies.mymovies

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
import com.michaldrabik.ui_model.MyMoviesSection
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.main.FollowedMoviesFragment
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_my_movies.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyMoviesFragment :
  BaseFragment<MyMoviesViewModel>(R.layout.fragment_my_movies),
  OnScrollResetListener,
  OnTraktSyncListener {

  override val viewModel by viewModels<MyMoviesViewModel>()

  private var adapter: MyMoviesAdapter? = null
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
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = MyMoviesAdapter(
      itemClickListener = { openMovieDetails(it.movie) },
      missingImageListener = { item, force -> viewModel.loadMissingImage(item, force) },
      missingTranslationListener = { viewModel.loadMissingTranslation(it) },
      onSortOrderClickListener = { section, order -> showSortOrderDialog(section, order) }
    )
    myMoviesRecycler.apply {
      adapter = this@MyMoviesFragment.adapter
      layoutManager = this@MyMoviesFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      myMoviesRoot.updatePadding(top = statusBarHeight)
      return
    }
    myMoviesRoot.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = statusBarHeight)
    }
  }

  private fun showSortOrderDialog(section: MyMoviesSection, order: SortOrder) {
    val options = listOf(NAME, RATING, NEWEST, DATE_ADDED)
    val optionsStrings = options.map { getString(it.displayString) }.toTypedArray()

    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setTitle(R.string.textSortBy)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setSingleChoiceItems(optionsStrings, options.indexOf(order)) { dialog, index ->
        viewModel.loadSortedSection(section, options[index])
        dialog.dismiss()
      }
      .show()
  }

  private fun render(uiState: MyMoviesUiState) {
    uiState.run {
      items?.let {
        adapter?.setItems(it)
        myMoviesEmptyView.fadeIf(it.isEmpty())
        (parentFragment as FollowedMoviesFragment).enableSearch(it.isNotEmpty())
      }
    }
  }

  private fun openMovieDetails(movie: Movie) {
    (parentFragment as? FollowedMoviesFragment)?.openMovieDetails(movie)
  }

  override fun onScrollReset() = myMoviesRecycler.scrollToPosition(0)

  override fun onTraktSyncComplete() = viewModel.loadMovies()

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
