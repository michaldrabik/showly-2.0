package com.michaldrabik.showly2.discover

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.showly2.BaseFragment
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.ViewModelFactory
import com.michaldrabik.showly2.appComponent

class DiscoverFragment : BaseFragment<DiscoverViewModel>() {

  companion object {
    @JvmStatic
    fun create() = DiscoverFragment()
  }

  override fun getLayoutResId() = R.layout.fragment_discover

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    appComponent().inject(this)
  }

  override fun createViewModel(factory: ViewModelFactory) =
    ViewModelProvider(this, viewModelFactory).get(DiscoverViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.loadTrendingShows()
  }
}
