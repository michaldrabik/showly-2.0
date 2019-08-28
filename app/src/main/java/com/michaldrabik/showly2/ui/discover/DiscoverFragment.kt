package com.michaldrabik.showly2.ui.discover

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ViewModelFactory
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.common.decorations.GridSpacingItemDecoration
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverAdapter
import com.michaldrabik.showly2.utilities.dimenToPx
import com.michaldrabik.showly2.utilities.visibleIf
import kotlinx.android.synthetic.main.fragment_discover.*

class DiscoverFragment : BaseFragment<DiscoverViewModel>() {

  private val adapter by lazy { DiscoverAdapter() }
  private val gridSpan by lazy { resources.getInteger(R.integer.discoverGridSpan) }
  private val layoutManager by lazy { GridLayoutManager(context, gridSpan) }

  override fun getLayoutResId() = R.layout.fragment_discover

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    appComponent().inject(this)
  }

  override fun createViewModel(factory: ViewModelFactory) =
    ViewModelProvider(this, viewModelFactory).get(DiscoverViewModel::class.java)
      .apply {
        uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupRecycler()
    viewModel.loadTrendingShows()
  }

  private fun setupRecycler() {
    discoverRecycler.apply {
      setHasFixedSize(true)
      addItemDecoration(
        GridSpacingItemDecoration(3, dimenToPx(R.dimen.gridSpacing))
      )
      adapter = this@DiscoverFragment.adapter
      layoutManager = this@DiscoverFragment.layoutManager
    }
    adapter.missingImageListener = {
      viewModel.loadMissingImage(it)
    }
  }

  private fun render(uiModel: DiscoverUiModel) {
    uiModel.trendingShows?.let { adapter.setItems(it) }
    uiModel.showLoading?.let { discoverProgress.visibleIf(it) }
    uiModel.missingImage?.let { adapter.updateItemImageUrl(it) }
  }
}
