package com.michaldrabik.ui_watchlist.upcoming

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.OnScrollResetListener
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_watchlist.R
import com.michaldrabik.ui_watchlist.di.UiWatchlistComponentProvider
import com.michaldrabik.ui_watchlist.main.WatchlistFragment
import com.michaldrabik.ui_watchlist.main.WatchlistViewModel
import com.michaldrabik.ui_watchlist.upcoming.recycler.WatchlistUpcomingAdapter
import kotlinx.android.synthetic.main.fragment_watchlist_upcoming.*

class WatchlistUpcomingFragment :
  BaseFragment<WatchlistUpcomingViewModel>(R.layout.fragment_watchlist_upcoming),
  OnScrollResetListener {

  private val parentViewModel by viewModels<WatchlistViewModel>({ requireParentFragment() }) { viewModelFactory }
  override val viewModel by viewModels<WatchlistUpcomingViewModel> { viewModelFactory }

  private var statusBarHeight = 0
  private lateinit var adapter: WatchlistUpcomingAdapter
  private lateinit var layoutManager: LinearLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiWatchlistComponentProvider).provideWatchlistComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupRecycler()
    setupStatusBar()

    parentViewModel.uiLiveData.observe(viewLifecycleOwner, { viewModel.handleParentAction(it) })
    viewModel.uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
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

  override fun onScrollReset() = watchlistUpcomingRecycler.smoothScrollToPosition(0)

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
