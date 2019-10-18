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
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment.Companion.ARG_SHOW_ID
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistAdapter
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import kotlinx.android.synthetic.main.fragment_watchlist.*

class WatchlistFragment : BaseFragment<WatchlistViewModel>() {

  override val layoutResId = R.layout.fragment_watchlist

  private lateinit var adapter: WatchlistAdapter
  private lateinit var layoutManager: LinearLayoutManager

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(WatchlistViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()

    viewModel.run {
      uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      watchlistStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      loadWatchlist()
    }
  }

  private fun setupView() {

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
    adapter.itemClickListener = { openShowDetails(it) }
  }

  private fun openShowDetails(item: WatchlistItem) {
    watchlistRoot.fadeOut {
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, item.show.id) }
      findNavController().navigate(R.id.actionWatchlistFragmentToShowDetailsFragment, bundle)
    }
    getMainActivity().hideNavigation()
  }

  private fun render(watchlistItems: List<WatchlistItem>) {
    adapter.setItems(watchlistItems)
  }

  private fun render(uiModel: WatchlistUiModel) {
    uiModel.run {
      error?.let {}
    }
  }
}
