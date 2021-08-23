package com.michaldrabik.ui_progress_movies.progress

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.core.view.updateMargins
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
import com.michaldrabik.ui_base.common.OnSortClickListener
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_base.common.views.RateView
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.NavigationHost
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesMainFragment
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesMainViewModel
import com.michaldrabik.ui_progress_movies.progress.recycler.ProgressMovieListItem
import com.michaldrabik.ui_progress_movies.progress.recycler.ProgressMoviesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_progress_movies_main.*
import kotlinx.android.synthetic.main.layout_progress_movies_empty.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProgressMoviesFragment :
  BaseFragment<ProgressMoviesViewModel>(R.layout.fragment_progress_movies_main),
  OnSortClickListener,
  OnScrollResetListener {

  private val parentViewModel by viewModels<ProgressMoviesMainViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<ProgressMoviesViewModel>()

  private var adapter: ProgressMoviesAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var statusBarHeight = 0

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()
    setupStatusBar()

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(parentViewModel) {
          launch { uiState.collect { viewModel.onParentState(it) } }
        }
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          launch { messageState.collect { showSnack(it) } }
          checkQuickRateEnabled()
        }
      }
    }
  }

  private fun setupView() {
    progressMoviesEmptyTraktButton.onClick { (parentFragment as ProgressMoviesMainFragment).openTraktSync() }
    progressMoviesEmptyDiscoverButton.onClick {
      (requireActivity() as NavigationHost).openDiscoverTab()
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = ProgressMoviesAdapter(
      itemClickListener = { (requireParentFragment() as ProgressMoviesMainFragment).openMovieDetails(it.movie) },
      itemLongClickListener = { item, view -> openPopupMenu(item, view) },
      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) },
      missingTranslationListener = { item -> viewModel.findMissingTranslation(item) },
      listChangeListener = {
        (requireParentFragment() as ProgressMoviesMainFragment).resetTranslations()
        layoutManager?.scrollToPosition(0)
      },
      checkClickListener = {
        if (viewModel.isQuickRateEnabled) {
          openRateDialog(it)
        } else {
          parentViewModel.setWatchedMovie(requireAppContext(), it.movie)
        }
      }
    )
    progressMoviesMainRecycler.apply {
      adapter = this@ProgressMoviesFragment.adapter
      layoutManager = this@ProgressMoviesFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      progressMoviesMainRecycler.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.progressMoviesTabsViewPadding))
      return
    }
    progressMoviesMainRecycler.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.progressMoviesTabsViewPadding))
      (progressMoviesEmptyView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarHeight + dimenToPx(R.dimen.spaceBig))
    }
  }

  private fun openPopupMenu(item: ProgressMovieListItem.MovieItem, view: View) {
    val menu = PopupMenu(requireContext(), view, Gravity.CENTER)
    if (item.isPinned) {
      menu.inflate(R.menu.progress_movies_item_menu_unpin)
    } else {
      menu.inflate(R.menu.progress_movies_item_menu_pin)
    }
    menu.setOnMenuItemClickListener { menuItem ->
      if (menuItem.itemId == R.id.menuProgressItemPin) {
        viewModel.togglePinItem(item)
      }
      true
    }
    menu.show()
  }

  private fun openRateDialog(item: ProgressMovieListItem.MovieItem) {
    val context = requireContext()
    val rateView = RateView(context).apply {
      setPadding(context.dimenToPx(R.dimen.spaceNormal))
      setRating(5)
    }
    MaterialAlertDialogBuilder(context, R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dialog))
      .setView(rateView)
      .setPositiveButton(R.string.textRate) { _, _ ->
        parentViewModel.setWatchedMovie(requireAppContext(), item.movie)
        viewModel.addRating(rateView.getRating(), item.movie)
      }
      .setNegativeButton(R.string.textCancel) { _, _ -> }
      .show()
  }

  private fun openSortOrderDialog(order: SortOrder) {
    val options = listOf(SortOrder.NAME, SortOrder.RATING, SortOrder.NEWEST, SortOrder.DATE_ADDED)
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

  override fun onScrollReset() = progressMoviesMainRecycler.smoothScrollToPosition(0)

  override fun onSortClick() = viewModel.loadSortOrder()

  private fun render(items: List<ProgressMovieListItem.MovieItem>, resetScroll: ActionEvent<Boolean>) {
    adapter?.setItems(items, notifyChange = resetScroll.consume() == true)
    progressMoviesEmptyView.fadeIf(items.isEmpty())
    progressMoviesMainRecycler.fadeIn(withHardware = true).add(animations)
    (requireAppContext() as WidgetsProvider).requestMoviesWidgetsUpdate()
  }

  private fun render(sortOrder: ActionEvent<SortOrder>) {
    sortOrder.consume()?.let { openSortOrderDialog(it) }
  }

  private fun render(uiState: ProgressMoviesUiState) {
    uiState.run {
      items?.let {
        val resetScroll = scrollReset?.consume() == true
        adapter?.setItems(it, resetScroll)
        progressMoviesEmptyView.fadeIf(items.isEmpty())
        progressMoviesMainRecycler.fadeIn(withHardware = true).add(animations)
        (requireAppContext() as WidgetsProvider).requestShowsWidgetsUpdate()
      }
      sortOrder?.let { event -> event.consume()?.let { openSortOrderDialog(it) } }
    }
  }

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
