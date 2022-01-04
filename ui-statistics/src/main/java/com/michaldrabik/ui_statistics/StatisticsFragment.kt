package com.michaldrabik.ui_statistics

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StatisticsFragment : BaseFragment<StatisticsViewModel>(R.layout.fragment_statistics) {

  override val viewModel by viewModels<StatisticsViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()

    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          if (!isInitialized) {
            loadData()
            isInitialized = true
          }
          loadRatings()
        }
      }
    }
  }

  private fun setupView() {
    statisticsToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    statisticsMostWatchedShows.run {
      onLoadMoreClickListener = { addLimit -> viewModel.loadData(addLimit) }
      onShowClickListener = {
        openShowDetails(it.traktId)
      }
    }
    statisticsRatings.onShowClickListener = {
      openShowDetails(it.show.traktId)
    }
  }

  private fun setupStatusBar() {
    statisticsRoot.doOnApplyWindowInsets { view, insets, padding, _ ->
      val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      view.updatePadding(top = padding.top + inset)
    }
  }

  private fun render(uiState: StatisticsUiState) {
    uiState.run {
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
    }
  }

  private fun openShowDetails(traktId: Long) {
    val bundle = bundleOf(ARG_SHOW_ID to traktId)
    navigateTo(R.id.actionStatisticsFragmentToShowDetailsFragment, bundle)
  }
}
