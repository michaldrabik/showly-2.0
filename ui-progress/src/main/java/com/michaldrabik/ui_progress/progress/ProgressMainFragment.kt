package com.michaldrabik.ui_progress.progress

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_base.utilities.NavigationHost
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Tip
import com.michaldrabik.ui_progress.ProgressItem
import com.michaldrabik.ui_progress.R
import com.michaldrabik.ui_progress.di.UiProgressComponentProvider
import com.michaldrabik.ui_progress.main.ProgressFragment
import com.michaldrabik.ui_progress.main.ProgressViewModel
import com.michaldrabik.ui_progress.progress.recycler.ProgressMainAdapter
import kotlinx.android.synthetic.main.fragment_progress_main.*
import kotlinx.android.synthetic.main.layout_progress_empty.*

class ProgressMainFragment :
  BaseFragment<ProgressMainViewModel>(R.layout.fragment_progress_main),
  OnScrollResetListener {

  private val parentViewModel by viewModels<ProgressViewModel>({ requireParentFragment() }) { viewModelFactory }
  override val viewModel by viewModels<ProgressMainViewModel> { viewModelFactory }

  private var statusBarHeight = 0
  private lateinit var adapter: ProgressMainAdapter
  private lateinit var layoutManager: LinearLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiProgressComponentProvider).provideProgressComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()
    setupStatusBar()

    parentViewModel.uiLiveData.observe(viewLifecycleOwner, { viewModel.handleParentAction(it) })
    viewModel.uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
  }

  private fun setupView() {
    progressEmptyTraktButton.onClick { (parentFragment as ProgressFragment).openTraktSync() }
    progressEmptyDiscoverButton.onClick {
      (requireActivity() as NavigationHost).openDiscoverTab()
    }
    progressMainTipItem.onClick {
      it.gone()
      showTip(Tip.WATCHLIST_ITEM_PIN)
    }
  }

  private fun setupRecycler() {
    adapter = ProgressMainAdapter()
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    progressMainRecycler.apply {
      adapter = this@ProgressMainFragment.adapter
      layoutManager = this@ProgressMainFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
    adapter.run {
      itemClickListener = { (requireParentFragment() as ProgressFragment).openShowDetails(it) }
      itemLongClickListener = { item, view -> openPopupMenu(item, view) }
      detailsClickListener = { (requireParentFragment() as ProgressFragment).openEpisodeDetails(it.show.ids.trakt, it.episode) }
      checkClickListener = { parentViewModel.setWatchedEpisode(requireAppContext(), it) }
      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) }
      missingTranslationListener = { viewModel.findMissingTranslation(it) }
      listChangeListener = {
        (requireParentFragment() as ProgressFragment).resetTranslations()
        layoutManager.scrollToPosition(0)
      }
    }
  }

  private fun setupStatusBar() {
    val recyclerPadding = if (moviesEnabled) R.dimen.progressTabsViewPadding else R.dimen.progressTabsViewPaddingNoModes
    if (statusBarHeight != 0) {
      progressMainRecycler.updatePadding(top = statusBarHeight + dimenToPx(recyclerPadding))
      return
    }
    progressMainRecycler.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = statusBarHeight + dimenToPx(recyclerPadding))
      (progressEmptyView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarHeight + dimenToPx(R.dimen.spaceBig))
    }
  }

  private fun openPopupMenu(item: ProgressItem, view: View) {
    val menu = PopupMenu(requireContext(), view, Gravity.CENTER)
    if (item.isPinned) {
      menu.inflate(R.menu.progress_item_menu_unpin)
    } else {
      menu.inflate(R.menu.progress_item_menu_pin)
    }
    menu.setOnMenuItemClickListener { menuItem ->
      if (menuItem.itemId == R.id.menuWatchlistItemPin) {
        parentViewModel.togglePinItem(item)
      }
      true
    }
    menu.show()
  }

  override fun onScrollReset() = progressMainRecycler.smoothScrollToPosition(0)

  private fun render(uiModel: ProgressMainUiModel) {
    uiModel.run {
      items?.let {
        adapter.setItems(it, notifyChange = resetScroll == true)
        progressEmptyView.fadeIf(it.isEmpty() && isSearching == false)
        progressMainRecycler.fadeIn()
        progressMainTipItem.visibleIf(it.count() >= 3 && !isTipShown(Tip.WATCHLIST_ITEM_PIN))
        (requireAppContext() as WidgetsProvider).run {
          requestShowsWidgetsUpdate()
          requestMoviesWidgetsUpdate()
        }
      }
    }
  }
}
