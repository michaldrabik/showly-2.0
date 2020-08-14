package com.michaldrabik.showly2.ui.watchlist.pages.watchlist

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.model.Tip
import com.michaldrabik.showly2.requireAppContext
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.watchlist.WatchlistFragment
import com.michaldrabik.showly2.ui.watchlist.WatchlistViewModel
import com.michaldrabik.showly2.ui.watchlist.pages.watchlist.recycler.WatchlistMainAdapter
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import com.michaldrabik.showly2.widget.watchlist.WatchlistWidgetProvider
import kotlinx.android.synthetic.main.fragment_watchlist_main.*
import kotlinx.android.synthetic.main.layout_watchlist_empty.*

class WatchlistMainFragment : BaseFragment<WatchlistMainViewModel>(R.layout.fragment_watchlist_main),
  OnTabReselectedListener {

  private val parentViewModel by viewModels<WatchlistViewModel>({ requireParentFragment() }) { viewModelFactory }
  override val viewModel by viewModels<WatchlistMainViewModel> { viewModelFactory }

  private var statusBarHeight = 0
  private lateinit var adapter: WatchlistMainAdapter
  private lateinit var layoutManager: LinearLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    fragmentComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()
    setupStatusBar()

    parentViewModel.uiLiveData.observe(viewLifecycleOwner, Observer { viewModel.handleParentAction(it) })
    viewModel.uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
  }

  private fun setupView() {
    watchlistEmptyTraktButton.onClick { (parentFragment as WatchlistFragment).openTraktSync() }
    watchlistEmptyDiscoverButton.onClick { mainActivity().openTab(R.id.menuDiscover) }
    watchlistMainTipItem.onClick {
      it.gone()
      mainActivity().showTip(Tip.WATCHLIST_ITEM_PIN)
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

  override fun onTabReselected() = watchlistMainRecycler.smoothScrollToPosition(0)

  private fun render(uiModel: WatchlistMainUiModel) {
    uiModel.run {
      items?.let {
        adapter.setItems(it)
        watchlistEmptyView.fadeIf(it.isEmpty() && isSearching == false)
        watchlistMainRecycler.fadeIn()
        watchlistMainTipItem.visibleIf(it.count() >= 3 && !mainActivity().isTipShown(Tip.WATCHLIST_ITEM_PIN))
        WatchlistWidgetProvider.requestUpdate(requireContext())
      }
    }
  }
}
