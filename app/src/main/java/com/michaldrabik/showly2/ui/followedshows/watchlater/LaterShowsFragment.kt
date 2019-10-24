package com.michaldrabik.showly2.ui.followedshows.watchlater

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment

class LaterShowsFragment : BaseFragment<LaterShowsViewModel>(), OnTabReselectedListener {

  override val layoutResId = R.layout.fragment_later_shows

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(LaterShowsViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
  }

  override fun onTabReselected() {
  }
}
