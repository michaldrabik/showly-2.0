package com.michaldrabik.showly2.ui.watchlist.pages.watchlist

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.PopupMenu
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
import com.michaldrabik.showly2.ui.show.seasons.episodes.details.EpisodeDetailsBottomSheet
import com.michaldrabik.showly2.ui.watchlist.WatchlistMainFragment
import com.michaldrabik.showly2.ui.watchlist.WatchlistMainViewModel
import com.michaldrabik.showly2.ui.watchlist.pages.watchlist.recycler.WatchlistAdapter
import com.michaldrabik.showly2.ui.watchlist.pages.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import com.michaldrabik.showly2.widget.watchlist.WatchlistWidgetProvider
import kotlinx.android.synthetic.main.fragment_watchlist.*

class WatchlistFragment : BaseFragment<WatchlistViewModel>(R.layout.fragment_watchlist),
  OnTabReselectedListener {

  private val parentViewModel by viewModels<WatchlistMainViewModel>({ requireParentFragment() }) { viewModelFactory }
  override val viewModel by viewModels<WatchlistViewModel> { viewModelFactory }

  private var statusBarHeight = 0
  private lateinit var adapter: WatchlistAdapter
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
    watchlistTipItem.onClick {
      it.gone()
      mainActivity().showTip(Tip.WATCHLIST_ITEM_PIN)
    }
  }

  private fun setupRecycler() {
    adapter = WatchlistAdapter()
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    watchlistRecycler.apply {
      adapter = this@WatchlistFragment.adapter
      layoutManager = this@WatchlistFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
    adapter.run {
      itemClickListener = { (requireParentFragment() as WatchlistMainFragment).openShowDetails(it) }
      itemLongClickListener = { item, view -> openPopupMenu(item, view) }
      detailsClickListener = { openEpisodeDetails(it) }
      checkClickListener = { parentViewModel.setWatchedEpisode(requireAppContext(), it) }
      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) }
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      watchlistRecycler.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.watchlistTabsViewPadding))
      return
    }
    watchlistRecycler.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.watchlistTabsViewPadding))
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

  private fun openEpisodeDetails(item: WatchlistItem) {
    val modal = EpisodeDetailsBottomSheet.create(
      item.show.ids.trakt,
      item.episode,
      isWatched = false,
      showButton = false
    )
    modal.show(requireActivity().supportFragmentManager, "MODAL")
  }

  override fun onTabReselected() = watchlistRecycler.smoothScrollToPosition(0)

  private fun render(uiModel: WatchlistUiModel) {
    uiModel.run {
      items?.let {
        adapter.setItems(it)
        watchlistRecycler.fadeIn()
        watchlistTipItem.visibleIf(it.count() >= 3 && !mainActivity().isTipShown(Tip.WATCHLIST_ITEM_PIN))
        WatchlistWidgetProvider.requestUpdate(requireContext())
      }
    }
  }
}
