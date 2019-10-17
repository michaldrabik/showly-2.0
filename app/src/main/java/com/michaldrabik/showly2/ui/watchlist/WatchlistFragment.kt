package com.michaldrabik.showly2.ui.watchlist

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.common.base.BaseFragment

class WatchlistFragment : BaseFragment<WatchlistViewModel>() {

  override val layoutResId = R.layout.fragment_watchlist

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(WatchlistViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    viewModel.uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
  }

  private fun setupView() {

  }

  private fun render(uiModel: WatchlistUiModel) {
    uiModel.run {
      error?.let {

      }
    }
  }

}
