package com.michaldrabik.ui_progress.progress

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
import com.michaldrabik.ui_base.utilities.NavigationHost
import com.michaldrabik.ui_base.utilities.extensions.add
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.EPISODES_LEFT
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RECENTLY_WATCHED
import com.michaldrabik.ui_model.Tip
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.main.ProgressMainFragment
import com.michaldrabik.ui_progress.main.ProgressMainViewModel
import com.michaldrabik.ui_progress.progress.recycler.ProgressAdapter
import com.michaldrabik.ui_progress.progress.recycler.ProgressListItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.android.synthetic.main.fragment_progress.*
import kotlinx.android.synthetic.main.layout_progress_empty.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProgressFragment :
  BaseFragment<ProgressViewModel>(R.layout.fragment_progress),
  OnSortClickListener,
  OnScrollResetListener {

  private val parentViewModel by viewModels<ProgressMainViewModel>({ requireParentFragment() })
  override val viewModel by viewModels<ProgressViewModel>()

  private var adapter: ProgressAdapter? = null
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
    progressEmptyTraktButton.onClick { (parentFragment as ProgressMainFragment).openTraktSync() }
    progressEmptyDiscoverButton.onClick {
      (requireActivity() as NavigationHost).openDiscoverTab()
    }
    progressTipItem.onClick {
      it.gone()
      showTip(Tip.WATCHLIST_ITEM_PIN)
    }
  }

  private fun setupRecycler() {
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    adapter = ProgressAdapter().apply {
      itemClickListener = {
        (requireParentFragment() as ProgressMainFragment).openShowDetails(it.show)
      }
      itemLongClickListener = { item, view ->
        openPopupMenu(item, view)
      }
      detailsClickListener = {
        (requireParentFragment() as ProgressMainFragment).openEpisodeDetails(it.show, it.requireEpisode(), it.requireSeason())
      }
      checkClickListener = {
        if (viewModel.isQuickRateEnabled) {
          openRateDialog(it)
        } else {
          val bundle = EpisodeBundle(it.episode!!, it.season!!, it.show)
          parentViewModel.setWatchedEpisode(requireAppContext(), bundle)
        }
      }
      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) }
      missingTranslationListener = { viewModel.findMissingTranslation(it) }
      listChangeListener = {
        (requireParentFragment() as ProgressMainFragment).resetTranslations()
        layoutManager?.scrollToPosition(0)
      }
    }
    progressRecycler.apply {
      adapter = this@ProgressFragment.adapter
      layoutManager = this@ProgressFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
  }

  private fun setupStatusBar() {
    val recyclerPadding = if (moviesEnabled) R.dimen.progressTabsViewPadding else R.dimen.progressTabsViewPaddingNoModes
    if (statusBarHeight != 0) {
      progressRecycler.updatePadding(top = statusBarHeight + dimenToPx(recyclerPadding))
      return
    }
    progressRecycler.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = statusBarHeight + dimenToPx(recyclerPadding))
      (progressEmptyView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarHeight + dimenToPx(R.dimen.spaceBig))
    }
  }

  private fun openPopupMenu(item: ProgressListItem.Episode, view: View) {
    val menu = PopupMenu(requireContext(), view, Gravity.CENTER)
    if (item.isPinned) {
      menu.inflate(R.menu.progress_item_menu_unpin)
    } else {
      menu.inflate(R.menu.progress_item_menu_pin)
    }
    menu.setOnMenuItemClickListener { menuItem ->
      if (menuItem.itemId == R.id.menuWatchlistItemPin) {
        viewModel.togglePinItem(item)
      }
      true
    }
    menu.show()
  }

  private fun openRateDialog(item: ProgressListItem.Episode) {
    val context = requireContext()
    val rateView = RateView(context).apply {
      setPadding(context.dimenToPx(R.dimen.spaceNormal))
      setRating(5)
    }
    MaterialAlertDialogBuilder(context, R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dialog))
      .setView(rateView)
      .setPositiveButton(R.string.textRate) { _, _ ->
        val bundle = EpisodeBundle(item.requireEpisode(), item.requireSeason(), item.show)
        parentViewModel.setWatchedEpisode(requireAppContext(), bundle)
        viewModel.addRating(rateView.getRating(), item.requireEpisode(), item.show.ids.trakt)
      }
      .setNegativeButton(R.string.textCancel) { _, _ -> }
      .show()
  }

  private fun openSortOrderDialog(order: SortOrder) {
    val options = listOf(NAME, NEWEST, RECENTLY_WATCHED, EPISODES_LEFT)
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

  private fun render(uiState: ProgressUiState) {
    uiState.run {
      items?.let {
        val resetScroll = scrollReset?.consume() == true
        adapter?.setItems(it, resetScroll)
        progressEmptyView.visibleIf(it.isEmpty() && !isLoading)
        progressTipItem.visibleIf(it.count() >= 3 && !isTipShown(Tip.WATCHLIST_ITEM_PIN))
        progressRecycler.fadeIn(withHardware = true).add(animations)
        (requireAppContext() as WidgetsProvider).requestShowsWidgetsUpdate()
      }
      sortOrder?.let { event -> event.consume()?.let { openSortOrderDialog(it) } }
    }
  }

  override fun onSortClick() = viewModel.loadSortOrder()

  override fun onScrollReset() = progressRecycler.smoothScrollToPosition(0)

  override fun setupBackPressed() = Unit

  override fun onDestroyView() {
    adapter = null
    layoutManager = null
    super.onDestroyView()
  }
}
