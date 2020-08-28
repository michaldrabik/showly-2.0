package com.michaldrabik.showly2.ui.followedshows.statistics

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.ui.common.OnScrollResetListener
import com.michaldrabik.showly2.ui.common.OnTabReselectedListener
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.followedshows.FollowedShowsFragment
import com.michaldrabik.showly2.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.fragment_statistics.*

class StatisticsFragment : BaseFragment<StatisticsViewModel>(R.layout.fragment_statistics),
  OnTabReselectedListener,
  OnScrollResetListener {

  override val viewModel by viewModels<StatisticsViewModel> { viewModelFactory }

  override fun onCreate(savedInstanceState: Bundle?) {
    fragmentComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
      if (!isInitialized) {
        loadMostWatchedShows()
        isInitialized = true
      }
      loadRatings()
    }
  }

  private fun setupView() {
    statisticsMostWatchedShows.run {
      onLoadMoreClickListener = { addLimit -> viewModel.loadMostWatchedShows(addLimit) }
      onShowClickListener = { (requireParentFragment() as FollowedShowsFragment).openShowDetails(it) }
    }
    statisticsRatings.onShowClickListener = {
      (requireParentFragment() as FollowedShowsFragment).openShowDetails(it.show)
    }
  }

  private fun setupStatusBar() {
    statisticsContent.doOnApplyWindowInsets { view, insets, padding, _ ->
      view.updatePadding(top = padding.top + insets.systemWindowInsetTop)
      statisticsEmptyView.updateLayoutParams {
        (statisticsEmptyView.layoutParams as ViewGroup.MarginLayoutParams)
          .updateMargins(top = insets.systemWindowInsetTop)
      }
    }
  }

  private fun render(uiModel: StatisticsUiModel) {
    uiModel.run {
      statisticsMostWatchedShows.bind(mostWatchedShows ?: emptyList(), mostWatchedTotalCount ?: 0)
      statisticsTotalTimeSpent.bind(totalTimeSpentMinutes ?: 0)
      statisticsTotalEpisodes.bind(totalWatchedEpisodes ?: 0, totalWatchedEpisodesShows ?: 0)
      statisticsTopGenres.bind(topGenres ?: emptyList())
      statisticsRatings.bind(ratings ?: emptyList())

      statisticsRatings.visibleIf(!ratings.isNullOrEmpty())
      statisticsContent.visibleIf(!mostWatchedShows.isNullOrEmpty())
      statisticsEmptyView.visibleIf(mostWatchedShows.isNullOrEmpty())
    }
  }

  override fun onTabReselected() = onScrollReset()

  override fun onScrollReset() = statisticsRoot.smoothScrollTo(0, 0)
}
