package com.michaldrabik.ui_watchlist.watchlist

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
import com.michaldrabik.ui_base.utilities.extensions.*
import com.michaldrabik.ui_model.Tip
import com.michaldrabik.ui_watchlist.R
import com.michaldrabik.ui_watchlist.WatchlistItem
import com.michaldrabik.ui_watchlist.di.UiWatchlistComponentProvider
import com.michaldrabik.ui_watchlist.main.WatchlistFragment
import com.michaldrabik.ui_watchlist.main.WatchlistViewModel
import com.michaldrabik.ui_watchlist.watchlist.recycler.WatchlistMainAdapter
import kotlinx.android.synthetic.main.fragment_watchlist_main.*
import kotlinx.android.synthetic.main.layout_watchlist_empty.*

class WatchlistMainFragment : BaseFragment<WatchlistMainViewModel>(R.layout.fragment_watchlist_main),
  OnScrollResetListener {

  private val parentViewModel by viewModels<WatchlistViewModel>({ requireParentFragment() }) { viewModelFactory }
  override val viewModel by viewModels<WatchlistMainViewModel> { viewModelFactory }

  private var statusBarHeight = 0
  private lateinit var adapter: WatchlistMainAdapter
  private lateinit var layoutManager: LinearLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiWatchlistComponentProvider).provideWatchlistComponent().inject(this)
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
    watchlistEmptyTraktButton.onClick { (parentFragment as WatchlistFragment).openTraktSync() }
    watchlistEmptyDiscoverButton.onClick {
      (requireActivity() as NavigationHost).openTab(R.id.menuDiscover)
    }
    watchlistMainTipItem.onClick {
      it.gone()
      showTip(Tip.WATCHLIST_ITEM_PIN)
    }
  }

  private fun setupRecycler() {
    adapter = WatchlistMainAdapter()
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    watchlistMainRecycler.apply {
      adapter = this@WatchlistMainFragment.adapter
      layoutManager = this@WatchlistMainFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
    adapter.run {
      itemClickListener = { (requireParentFragment() as WatchlistFragment).openShowDetails(it) }
      itemLongClickListener = { item, view -> openPopupMenu(item, view) }
      detailsClickListener = { (requireParentFragment() as WatchlistFragment).openEpisodeDetails(it.show.ids.trakt, it.episode) }
      checkClickListener = { parentViewModel.setWatchedEpisode(requireAppContext(), it) }
      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) }
      listChangeListener = {
        (requireParentFragment() as WatchlistFragment).resetTranslations()
        layoutManager.scrollToPosition(0)
      }
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      watchlistMainRecycler.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.watchlistTabsViewPadding))
      return
    }
    watchlistMainRecycler.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.watchlistTabsViewPadding))
      (watchlistEmptyView.layoutParams as ViewGroup.MarginLayoutParams)
        .updateMargins(top = statusBarHeight + dimenToPx(R.dimen.spaceBig))
    }
  }

  private fun openPopupMenu(item: WatchlistItem, view: View) {
    val menu = PopupMenu(requireContext(), view, Gravity.CENTER)
    if (item.isPinned) {
      menu.inflate(R.menu.watchlist_item_menu_unpin)
    } else {
      menu.inflate(R.menu.watchlist_item_menu_pin)
    }
    menu.setOnMenuItemClickListener { menuItem ->
      if (menuItem.itemId == R.id.menuWatchlistItemPin) {
        parentViewModel.togglePinItem(item)
      }
      true
    }
    menu.show()
  }

  override fun onScrollReset() = watchlistMainRecycler.smoothScrollToPosition(0)

  private fun render(uiModel: WatchlistMainUiModel) {
    uiModel.run {
      items?.let {
        adapter.setItems(it, notifyChange = resetScroll == true)
        watchlistEmptyView.fadeIf(it.isEmpty() && isSearching == false)
        watchlistMainRecycler.fadeIn()
        watchlistMainTipItem.visibleIf(it.count() >= 3 && !isTipShown(Tip.WATCHLIST_ITEM_PIN))
        (requireAppContext() as WidgetsProvider).requestWidgetsUpdate()
      }
    }
  }
}
