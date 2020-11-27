package com.michaldrabik.ui_discover_movies

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.showly2.R
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_discover_movies.cases.DiscoverFiltersCase
import com.michaldrabik.ui_discover_movies.cases.DiscoverMoviesCase
import com.michaldrabik.ui_model.DiscoverFilters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DiscoverMoviesViewModel @Inject constructor(
  private val moviesCase: DiscoverMoviesCase,
  private val filtersCase: DiscoverFiltersCase,
  private val imagesProvider: ShowImagesProvider
) : BaseViewModel<DiscoverMoviesUiModel>() {

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  var lastPullToRefreshMs = 0L

  fun loadMovies(
    pullToRefresh: Boolean = false,
    resetScroll: Boolean = false,
    skipCache: Boolean = false,
    instantProgress: Boolean = false,
    newFilters: DiscoverFilters? = null
  ) {
    if (pullToRefresh && nowUtcMillis() - lastPullToRefreshMs < Config.PULL_TO_REFRESH_COOLDOWN_MS) {
      uiState = DiscoverMoviesUiModel(showLoading = false)
      return
    }

    uiState = DiscoverMoviesUiModel(showLoading = pullToRefresh)

    viewModelScope.launch {
      val progressJob = launch {
        delay(if (pullToRefresh || instantProgress) 0 else 750)
        uiState = DiscoverMoviesUiModel(showLoading = true)
      }

      try {
        newFilters?.let { filtersCase.saveFilters(it) }
        val filters = filtersCase.loadFilters()
        uiState = DiscoverMoviesUiModel(filters = filters)

        if (!pullToRefresh && !skipCache) {
          val movies = moviesCase.loadCachedMovies(filters)
          uiState = DiscoverMoviesUiModel(movies = movies, filters = filters, resetScroll = resetScroll)
        }

        if (pullToRefresh || skipCache || !moviesCase.isCacheValid()) {
          val shows = moviesCase.loadRemoteMovies(filters)
          uiState = DiscoverMoviesUiModel(movies = shows, filters = filters, resetScroll = resetScroll)
        }

        if (pullToRefresh) {
          lastPullToRefreshMs = nowUtcMillis()
        }
      } catch (error: Throwable) {
        onError(error)
      } finally {
        uiState = DiscoverMoviesUiModel(showLoading = false)
        progressJob.cancel()
      }
    }
  }

  private fun onError(error: Throwable) {
    if (error !is CancellationException) {
      _messageLiveData.value = MessageEvent.error(R.string.errorCouldNotLoadDiscover)
      Timber.e(error)
    }
  }
}
