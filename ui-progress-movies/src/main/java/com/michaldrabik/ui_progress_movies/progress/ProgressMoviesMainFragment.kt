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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_base.common.views.RateView
import com.michaldrabik.ui_base.utilities.NavigationHost
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_progress_movies.ProgressMovieItem
import com.michaldrabik.ui_progress_movies.R
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesFragment
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesViewModel
import com.michaldrabik.ui_progress_movies.progress.recycler.ProgressMainAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_progress_movies_main.*
import kotlinx.android.synthetic.main.layout_progress_movies_empty.*

@AndroidEntryPoint
class ProgressMoviesMainFragment :
  BaseFragment<ProgressMoviesMainViewModel>(R.layout.fragment_progress_movies_main),
  OnScrollResetListener {

  private val parentViewModel by viewModels<ProgressMoviesViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<ProgressMoviesMainViewModel>()

  private var adapter: ProgressMainAdapter? = null
  private var layoutManager: LinearLayoutManager? = null
  private var statusBarHeight = 0

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()
    setupStatusBar()

    parentViewModel.uiLiveData.observe(viewLifecycleOwner, { viewModel.handleParentAction(it) })
    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      messageLiveData.observe(viewLifecycleOwner, { showSnack(it) })
      checkQuickRateEnabled()
    }
  }

  private fun setupView() {
    progressMoviesEmptyTraktButton.onClick { (parentFragment as ProgressMoviesFragment).openTraktSync() }
    progressMoviesEmptyDiscoverButton.onClick {
      (requireActivity() as NavigationHost).openDiscoverTab()
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = ProgressMainAdapter(
      itemClickListener = { (requireParentFragment() as ProgressMoviesFragment).openMovieDetails(it) },
      itemLongClickListener = { item, view -> openPopupMenu(item, view) },
      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) },
      missingTranslationListener = { item -> viewModel.findMissingTranslation(item) },
      listChangeListener = {
        (requireParentFragment() as ProgressMoviesFragment).resetTranslations()
        layoutManager?.scrollToPosition(0)
      },
      checkClickListener = {
        if (viewModel.isQuickRateEnabled) {
          openRateDialog(it)
        } else {
          parentViewModel.addWatchedMovie(requireAppContext(), it)
        }
      }
    )
    progressMoviesMainRecycler.apply {
      adapter = this@ProgressMoviesMainFragment.adapter
      layoutManager = this@ProgressMoviesMainFragment.layoutManager
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

  private fun openPopupMenu(item: ProgressMovieItem, view: View) {
    val menu = PopupMenu(requireContext(), view, Gravity.CENTER)
    if (item.isPinned) {
      menu.inflate(R.menu.progress_movies_item_menu_unpin)
    } else {
      menu.inflate(R.menu.progress_movies_item_menu_pin)
    }
    menu.setOnMenuItemClickListener { menuItem ->
      if (menuItem.itemId == R.id.menuProgressItemPin) {
        parentViewModel.togglePinItem(item)
      }
      true
    }
    menu.show()
  }

  private fun openRateDialog(item: ProgressMovieItem) {
    val context = requireContext()
    val rateView = RateView(context).apply {
      setPadding(context.dimenToPx(R.dimen.spaceNormal))
      setRating(5)
    }
    MaterialAlertDialogBuilder(context, R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dialog))
      .setView(rateView)
      .setPositiveButton(R.string.textRate) { _, _ ->
        parentViewModel.addWatchedMovie(requireAppContext(), item)
        viewModel.addRating(rateView.getRating(), item.movie)
      }
      .setNegativeButton(R.string.textCancel) { _, _ -> }
      .show()
  }

  override fun onScrollReset() = progressMoviesMainRecycler.smoothScrollToPosition(0)

  private fun render(uiModel: ProgressMoviesMainUiModel) {
    uiModel.run {
      items?.let {
        val notifyChange = resetScroll?.consume() == true
        adapter?.setItems(it, notifyChange = notifyChange)
        progressMoviesEmptyView.fadeIf(it.isEmpty() && isSearching == false)
        progressMoviesMainRecycler.fadeIn()
        (requireAppContext() as WidgetsProvider).requestMoviesWidgetsUpdate()
      }
    }
  }

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
