package com.michaldrabik.ui_my_movies.hidden

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.OnTraktSyncListener
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
import com.michaldrabik.ui_my_movies.R
import com.michaldrabik.ui_my_movies.hidden.recycler.HiddenAdapter
import com.michaldrabik.ui_my_movies.main.FollowedMoviesFragment
import com.michaldrabik.ui_my_movies.utilities.OnSortClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_hidden_movies.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class HiddenFragment :
  BaseFragment<HiddenViewModel>(R.layout.fragment_hidden_movies),
  OnScrollResetListener,
  OnTraktSyncListener,
  OnSortClickListener {

  override val viewModel by viewModels<HiddenViewModel>()

  private var adapter: HiddenAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var statusBarHeight = 0

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupStatusBar()
    setupRecycler()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      afterBlock = { viewModel.loadMovies() }
    )
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
    adapter = HiddenAdapter(
      itemClickListener = { openMovieDetails(it.movie) },
      missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) },
      missingTranslationListener = { viewModel.loadMissingTranslation(it) },
      listChangeListener = {
        hiddenMoviesRecycler.scrollToPosition(0)
        (requireParentFragment() as FollowedMoviesFragment).resetTranslations()
      }
    )
    hiddenMoviesRecycler.apply {
      setHasFixedSize(true)
      adapter = this@HiddenFragment.adapter
      layoutManager = this@HiddenFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      hiddenMoviesContent.updatePadding(top = hiddenMoviesContent.paddingTop + statusBarHeight)
      hiddenMoviesRecycler.updatePadding(top = dimenToPx(R.dimen.hiddenTabsViewPadding))
      return
    }
    hiddenMoviesContent.doOnApplyWindowInsets { view, insets, padding, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = padding.top + statusBarHeight)
      hiddenMoviesRecycler.updatePadding(top = dimenToPx(R.dimen.hiddenTabsViewPadding))
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

  private fun render(uiState: HiddenUiState) {
    uiState.run {
      items.let {
        val notifyChange = resetScroll?.consume() == true
        adapter?.setItems(it, notifyChange = notifyChange)
        hiddenMoviesEmptyView.fadeIf(it.isEmpty())
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

  override fun onScrollReset() = hiddenMoviesRecycler.scrollToPosition(0)

  override fun onTraktSyncComplete() = viewModel.loadMovies()

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
