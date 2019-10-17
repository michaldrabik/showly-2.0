package com.michaldrabik.showly2.ui.watchlist

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistAdapter
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

    viewModel.uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
  }

  override fun onResume() {
    super.onResume()
    viewModel.loadWatchlist()
  }

  private fun setupView() {

  }

  private fun setupRecycler() {
    adapter = WatchlistAdapter()
    layoutManager = LinearLayoutManager(context, VERTICAL, false)
    watchlistRecycler.apply {
      adapter = this@WatchlistFragment.adapter
      layoutManager = this@WatchlistFragment.layoutManager
      setHasFixedSize(true)
      addItemDecoration(DividerItemDecoration(context, VERTICAL).apply {
        setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_watchlist)!!)
      })
    }
  }

  private fun render(uiModel: WatchlistUiModel) {
    uiModel.run {
      watchlistItems?.let { adapter.setItems(it) }
      error?.let {}
    }
  }

}
