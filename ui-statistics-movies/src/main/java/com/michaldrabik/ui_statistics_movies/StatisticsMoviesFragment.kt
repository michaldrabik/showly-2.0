package com.michaldrabik.ui_statistics_movies

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_statistics_movies.di.UiStatisticsMoviesComponentProvider
import kotlinx.android.synthetic.main.fragment_statistics_movies.*

class StatisticsMoviesFragment : BaseFragment<StatisticsMoviesViewModel>(R.layout.fragment_statistics_movies) {

  override val viewModel by viewModels<StatisticsMoviesViewModel> { viewModelFactory }

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiStatisticsMoviesComponentProvider).provideStatisticsMoviesComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupStatusBar()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      if (!isInitialized) {
        loadMovies()
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
    statisticsMoviesToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    statisticsMoviesRatings.onMovieClickListener = {
      openMovieDetails(it.movie.traktId)
    }
  }

  private fun setupStatusBar() {
    statisticsMoviesRoot.doOnApplyWindowInsets { view, insets, padding, _ ->
      view.updatePadding(top = padding.top + insets.systemWindowInsetTop)
    }
  }

  private fun render(uiModel: StatisticsMoviesUiModel) {
    uiModel.run {
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

  private fun handleBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      remove()
      showNavigation()
      findNavControl().popBackStack()
    }
  }
}
