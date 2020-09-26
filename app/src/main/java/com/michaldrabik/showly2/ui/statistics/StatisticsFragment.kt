package com.michaldrabik.showly2.ui.statistics

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.followedshows.FollowedShowsFragment
import com.michaldrabik.showly2.ui.show.ShowDetailsFragment
import com.michaldrabik.showly2.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import kotlinx.android.synthetic.main.fragment_statistics.*

class StatisticsFragment : BaseFragment<StatisticsViewModel>(R.layout.fragment_statistics) {

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
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      if (!isInitialized) {
        loadMostWatchedShows()
        isInitialized = true
      }
      loadRatings()
    }
  }

  override fun onResume() {
    super.onResume()
    hideNavigation()
    handleBackPressed()
  }

  private fun setupView() {
    statisticsToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    statisticsMostWatchedShows.run {
      onLoadMoreClickListener = { addLimit -> viewModel.loadMostWatchedShows(addLimit) }
      onShowClickListener = { (requireParentFragment() as FollowedShowsFragment).openShowDetails(it) }
    }
    statisticsRatings.onShowClickListener = {
      val bundle = bundleOf(ShowDetailsFragment.ARG_SHOW_ID to it.show.traktId)
      navigateTo(R.id.actionStatisticsFragmentToShowDetailsFragment, bundle)
    }
  }

  private fun setupStatusBar() {
    statisticsRoot.doOnApplyWindowInsets { view, insets, padding, _ ->
      view.updatePadding(top = padding.top + insets.systemWindowInsetTop)
    }
  }

  private fun render(uiModel: StatisticsUiModel) {
    uiModel.run {
      statisticsMostWatchedShows.bind(mostWatchedShows ?: emptyList(), mostWatchedTotalCount ?: 0)
      statisticsTotalTimeSpent.bind(totalTimeSpentMinutes ?: 0)
      statisticsTotalEpisodes.bind(totalWatchedEpisodes ?: 0, totalWatchedEpisodesShows ?: 0)
      statisticsTopGenres.bind(topGenres ?: emptyList())
      statisticsRatings.bind(ratings ?: emptyList())

      ratings?.let { statisticsRatings.visibleIf(it.isNotEmpty()) }
      mostWatchedShows?.let {
        statisticsContent.fadeIf(it.isNotEmpty())
        statisticsEmptyView.fadeIf(it.isEmpty())
      }
      archivedShowsIncluded?.let {
        if (!it) {
          statisticsToolbar.subtitle = getString(R.string.textArchivedShowsExcluded)
        }
      }
    }
  }

  private fun handleBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      remove()
      showNavigation()
      findNavController().popBackStack()
    }
  }
}
