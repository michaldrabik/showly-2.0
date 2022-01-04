package com.michaldrabik.ui_statistics_movies

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
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics_movies.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StatisticsMoviesFragment : BaseFragment<StatisticsMoviesViewModel>(R.layout.fragment_statistics_movies) {

  override val viewModel by viewModels<StatisticsMoviesViewModel>()

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
    statisticsMoviesToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    statisticsMoviesRatings.onMovieClickListener = {
      openMovieDetails(it.movie.traktId)
    }
  }

  private fun setupStatusBar() {
    statisticsMoviesRoot.doOnApplyWindowInsets { view, insets, padding, _ ->
      val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      view.updatePadding(top = padding.top + inset)
    }
  }

  private fun render(uiState: StatisticsMoviesUiState) {
    uiState.run {
      statisticsMoviesTotalTimeSpent.bind(totalTimeSpentMinutes ?: 0)
      statisticsMoviesTotalMovies.bind(totalWatchedMovies ?: 0)
      statisticsMoviesTopGenres.bind(topGenres ?: emptyList())
      statisticsMoviesRatings.bind(ratings ?: emptyList())
      ratings?.let { statisticsMoviesRatings.visibleIf(it.isNotEmpty()) }
      totalWatchedMovies?.let {
        statisticsMoviesContent.fadeIf(it > 0)
        statisticsMoviesEmptyView.fadeIf(it <= 0)
      }
    }
  }

  private fun openMovieDetails(traktId: Long) {
    val bundle = bundleOf(ARG_MOVIE_ID to traktId)
    navigateTo(R.id.actionStatisticsMoviesFragmentToMovieDetailsFragment, bundle)
  }
}
