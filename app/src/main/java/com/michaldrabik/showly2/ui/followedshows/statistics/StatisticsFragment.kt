package com.michaldrabik.showly2.ui.followedshows.statistics

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.showly2.utilities.extensions.visibleIf
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
    setupStatusBar()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
      if (!isInitialized) {
        loadMostWatchedShows()
        isInitialized = true
      }
    }
  }

  private fun setupStatusBar() {
    statisticsContent.doOnApplyWindowInsets { view, insets, padding, _ ->
      view.updatePadding(top = padding.top + insets.systemWindowInsetTop)
    }
  }

  private fun render(uiModel: StatisticsUiModel) {
    uiModel.run {
      statisticsMostWatchedShows.bind(mostWatchedShows ?: emptyList())
      statisticsTotalTimeSpent.bind(totalTimeSpentMinutes ?: 0)
      statisticsTotalEpisodes.bind(totalWatchedEpisodes ?: 0, totalWatchedEpisodesShows ?: 0)
      statisticsTopGenres.bind(topGenres ?: emptyList())

      statisticsContent.visibleIf(!mostWatchedShows.isNullOrEmpty())
      statisticsEmptyView.visibleIf(mostWatchedShows.isNullOrEmpty())
    }
  }
}
