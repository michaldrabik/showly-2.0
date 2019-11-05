package com.michaldrabik.showly2.ui.watchlist

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.common.OnEpisodesSyncedListener
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.ui.show.seasons.episodes.details.EpisodeDetailsBottomSheet
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistAdapter
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.showErrorSnackbar
import com.michaldrabik.showly2.utilities.extensions.showShortInfoSnackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_watchlist.*

class WatchlistFragment : BaseFragment<WatchlistViewModel>(), OnTabReselectedListener, OnEpisodesSyncedListener {

  override val layoutResId = R.layout.fragment_watchlist

  private lateinit var adapter: WatchlistAdapter
  private lateinit var layoutManager: LinearLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel(provider: ViewModelProvider) =
    provider.get(WatchlistViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupRecycler()

    viewModel.run {
      uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      watchlistStream.observe(viewLifecycleOwner, Observer { render(it!!) })
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.loadWatchlist()
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
      itemClickListener = { openShowDetails(it) }
      detailsClickListener = { openEpisodeDetails(it) }
      checkClickListener = { viewModel.setWatchedEpisode(it) }
      missingImageListener = { item, force -> viewModel.findMissingImage(item, force) }
    }
  }

  private fun openShowDetails(item: WatchlistItem) {
    hideNavigation()
    watchlistRoot.fadeOut {
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, item.show.ids.trakt.id) }
      findNavController().navigate(R.id.actionWatchlistFragmentToShowDetailsFragment, bundle)
    }
  }

  private fun openEpisodeDetails(item: WatchlistItem) {
    val modal = EpisodeDetailsBottomSheet.create(item.episode, isWatched = false, showButton = false)
    modal.show(requireActivity().supportFragmentManager, "MODAL")
  }

  override fun onTabReselected() = watchlistRecycler.smoothScrollToPosition(0)

  override fun onEpisodesSyncSuccess() = viewModel.loadWatchlist()

  private fun render(watchlistItems: List<WatchlistItem>) {
    adapter.setItems(watchlistItems)
    watchlistRecycler.fadeIn()
    watchlistEmptyView.fadeIf(watchlistItems.isEmpty())
  }

  private fun render(uiModel: WatchlistUiModel) {
    uiModel.run {
      updateListItem?.let { adapter.updateItem(it) }
      info?.let { requireActivity().snackBarHost.showShortInfoSnackbar(getString(it)) }
      error?.let { requireActivity().snackBarHost.showErrorSnackbar(it.message ?: getString(R.string.errorGeneral)) }
    }
  }
}
