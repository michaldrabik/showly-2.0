package com.michaldrabik.showly2.ui.followedshows.statistics

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_statistics.*

class StatisticsFragment : BaseFragment<StatisticsViewModel>(R.layout.fragment_statistics) {

  override val viewModel by viewModels<StatisticsViewModel> { viewModelFactory }

  private var isInitialized = false

  override fun onCreate(savedInstanceState: Bundle?) {
    fragmentComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
      if (!isInitialized) {
        loadMostWatchedShows()
        isInitialized = true
      }
    }
  }

  private fun render(uiModel: StatisticsUiModel) {
    uiModel.run {
      statisticsMostWatchedShows.bind(mostWatchedShows ?: emptyList())
      statisticsTotalTimeSpent.bind(uiModel.totalTimeSpentMinutes ?: 0)
    }
  }
}
