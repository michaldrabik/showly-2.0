package com.michaldrabik.showly2.ui.watchlist.pages.upcoming

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.watchlist.WatchlistFragment
import com.michaldrabik.showly2.ui.watchlist.WatchlistViewModel
import com.michaldrabik.showly2.ui.watchlist.pages.upcoming.recycler.WatchlistUpcomingAdapter
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.fragment_watchlist_upcoming.*

class WatchlistUpcomingFragment : BaseFragment<WatchlistUpcomingViewModel>(R.layout.fragment_watchlist_upcoming),
  OnTabReselectedListener {

  private val parentViewModel by viewModels<WatchlistViewModel>({ requireParentFragment() }) { viewModelFactory }
  override val viewModel by viewModels<WatchlistUpcomingViewModel> { viewModelFactory }

  private var statusBarHeight = 0
  private lateinit var adapter: WatchlistUpcomingAdapter
  private lateinit var layoutManager: LinearLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    fragmentComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupRecycler()
    setupStatusBar()

    parentViewModel.uiLiveData.observe(viewLifecycleOwner, Observer { viewModel.handleParentAction(it) })
    viewModel.uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
  }

  private fun setupRecycler() {
    adapter = WatchlistUpcomingAdapter()
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    watchlistUpcomingRecycler.apply {
      adapter = this@WatchlistUpcomingFragment.adapter
      layoutManager = this@WatchlistUpcomingFragment.layoutManager
      (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
      setHasFixedSize(true)
    }
    adapter.run {
      itemClickListener = { (requireParentFragment() as WatchlistFragment).openShowDetails(it) }
      detailsClickListener = { (requireParentFragment() as WatchlistFragment).openEpisodeDetails(it.show.ids.trakt, it.upcomingEpisode) }
      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) }
    }
  }

  private fun setupStatusBar() {
    if (statusBarHeight != 0) {
      watchlistUpcomingRecycler.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.watchlistUpcomingTabsViewPadding))
      return
    }
    watchlistUpcomingRecycler.doOnApplyWindowInsets { view, insets, _, _ ->
      statusBarHeight = insets.systemWindowInsetTop
      view.updatePadding(top = statusBarHeight + dimenToPx(R.dimen.watchlistUpcomingTabsViewPadding))
    }
  }

  override fun onTabReselected() = watchlistUpcomingRecycler.smoothScrollToPosition(0)

  private fun render(uiModel: WatchlistUpcomingUiModel) {
    uiModel.run {
      items?.let {
        adapter.setItems(it)
        watchlistUpcomingRecycler.fadeIn()
        watchlistUpcomingEmptyView.visibleIf(it.isEmpty())
      }
    }
  }
}
