package com.michaldrabik.showly2.ui.watchlist.pages.upcoming

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.watchlist.WatchlistMainViewModel
import com.michaldrabik.showly2.ui.watchlist.pages.watchlist.recycler.WatchlistAdapter
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.doOnApplyWindowInsets
import kotlinx.android.synthetic.main.fragment_watchlist.*
import kotlinx.android.synthetic.main.fragment_watchlist_upcoming.*

class WatchlistUpcomingFragment : BaseFragment<WatchlistUpcomingViewModel>(R.layout.fragment_watchlist_upcoming),
  OnTabReselectedListener {

  private val parentViewModel by viewModels<WatchlistMainViewModel>({ requireParentFragment() }) { viewModelFactory }
  override val viewModel by viewModels<WatchlistUpcomingViewModel> { viewModelFactory }

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
//    setupRecycler()
    setupStatusBar()

    parentViewModel.uiLiveData.observe(viewLifecycleOwner, Observer { viewModel.handleParentAction(it) })
    viewModel.uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
  }

  private fun setupView() {

  }

//  private fun setupRecycler() {
//    adapter = WatchlistAdapter()
//    layoutManager = LinearLayoutManager(context, VERTICAL, false)
//    watchlistRecycler.apply {
//      adapter = this@WatchlistUpcomingFragment.adapter
//      layoutManager = this@WatchlistUpcomingFragment.layoutManager
//      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
//      setHasFixedSize(true)
//    }
//    adapter.run {
//      itemClickListener = { (requireParentFragment() as WatchlistMainFragment).openShowDetails(it) }
//      itemLongClickListener = { item, view -> openPopupMenu(item, view) }
//      detailsClickListener = { openEpisodeDetails(it) }
//      checkClickListener = { parentViewModel.setWatchedEpisode(requireAppContext(), it) }
//      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) }
//    }
//  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      watchlistUpcomingRecycler.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.watchlistTabsViewPadding))
      return
    }
    watchlistUpcomingRecycler.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.watchlistTabsViewPadding))
    }
  }

  override fun onTabReselected() = watchlistRecycler.smoothScrollToPosition(0)

  private fun render(uiModel: WatchlistUpcomingUiModel) {
    uiModel.run {
      items?.let {
//        adapter.setItems(it)
//        watchlistRecycler.fadeIn()
//        watchlistTipItem.visibleIf(it.count() >= 3 && !mainActivity().isTipShown(Tip.WATCHLIST_ITEM_PIN))
//        WatchlistWidgetProvider.requestUpdate(requireContext())
      }
    }
  }
}
