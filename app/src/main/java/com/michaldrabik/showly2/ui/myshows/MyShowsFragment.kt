package com.michaldrabik.showly2.ui.myshows

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.common.base.BaseFragment

class MyShowsFragment : BaseFragment<MyShowsViewModel>() {

  override val layoutResId = R.layout.fragment_my_shows

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(MyShowsViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

//    viewModel.uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
//    viewModel.loadTrendingShows()
  }

  private fun setupView() {

  }

}
