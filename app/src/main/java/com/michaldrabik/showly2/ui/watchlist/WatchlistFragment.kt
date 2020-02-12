package com.michaldrabik.showly2.ui.watchlist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.common.OnEpisodesSyncedListener
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.OnTraktImportListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.ui.show.seasons.episodes.details.EpisodeDetailsBottomSheet
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistAdapter
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.onClick
import kotlinx.android.synthetic.main.fragment_watchlist.*
import kotlinx.android.synthetic.main.layout_watchlist_empty.*

class WatchlistFragment : BaseFragment<WatchlistViewModel>(), OnTabReselectedListener, OnEpisodesSyncedListener, OnTraktImportListener {

  override val layoutResId = R.layout.fragment_watchlist
  override val viewModel by viewModels<WatchlistViewModel> { viewModelFactory }

  private lateinit var adapter: WatchlistAdapter
  private lateinit var layoutManager: LinearLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
      messageLiveData.observe(viewLifecycleOwner, Observer { showInfoSnackbar(it!!) })
    }
  }

  override fun onResume() {
    super.onResume()
    showNavigation()
    viewModel.loadWatchlist()
  }

  private fun setupView() {
    watchlistEmptyTraktButton.onClick { openTraktImport() }
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
      navigateTo(R.id.actionWatchlistFragmentToShowDetailsFragment, bundle)
    }
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

  private fun openTraktImport() {
    navigateTo(R.id.actionWatchlistFragmentToTraktImportFragment)
    hideNavigation()
  }

  override fun onTabReselected() = watchlistRecycler.smoothScrollToPosition(0)

  override fun onEpisodesSyncFinished() = viewModel.loadWatchlist()

  override fun onTraktImportProgress() = viewModel.loadWatchlist()

  private fun render(uiModel: WatchlistUiModel) {
    uiModel.items?.let {
      adapter.setItems(it)
      watchlistRecycler.fadeIn()
      watchlistEmptyView.fadeIf(it.isEmpty())
    }
  }
}
