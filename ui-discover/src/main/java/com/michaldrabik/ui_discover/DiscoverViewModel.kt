package com.michaldrabik.ui_discover

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_discover.cases.DiscoverFiltersCase
import com.michaldrabik.ui_discover.cases.DiscoverShowsCase
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.Image
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
  private val showsCase: DiscoverShowsCase,
  private val filtersCase: DiscoverFiltersCase,
  private val imagesProvider: ShowImagesProvider
) : BaseViewModel<DiscoverUiModel>() {

  @VisibleForTesting(otherwise = PRIVATE)
  var lastPullToRefreshMs = 0L

  fun loadDiscoverShows(
    pullToRefresh: Boolean = false,
    scrollToTop: Boolean = false,
    skipCache: Boolean = false,
    instantProgress: Boolean = false,
    newFilters: DiscoverFilters? = null
  ) {
    if (pullToRefresh && nowUtcMillis() - lastPullToRefreshMs < Config.PULL_TO_REFRESH_COOLDOWN_MS) {
      uiState = DiscoverUiModel(showLoading = false)
      return
    }

    uiState = DiscoverUiModel(showLoading = pullToRefresh)

    viewModelScope.launch {
      val progressJob = launch {
        delay(if (pullToRefresh || instantProgress) 0 else 750)
        uiState = DiscoverUiModel(showLoading = true)
      }

      try {
        newFilters?.let { filtersCase.saveFilters(it) }
        val filters = filtersCase.loadFilters()
        uiState = DiscoverUiModel(filters = filters)

        if (!pullToRefresh && !skipCache) {
          val shows = showsCase.loadCachedShows(filters)
          uiState = DiscoverUiModel(shows = shows, filters = filters, scrollToTop = scrollToTop)
        }

        if (pullToRefresh || skipCache || !showsCase.isCacheValid()) {
          val shows = showsCase.loadRemoteShows(filters)
          uiState = DiscoverUiModel(shows = shows, filters = filters, scrollToTop = scrollToTop)
        }

        if (pullToRefresh) {
          lastPullToRefreshMs = nowUtcMillis()
        }
      } catch (error: Throwable) {
        onError(error)
      } finally {
        uiState = DiscoverUiModel(showLoading = false)
        progressJob.cancel()
      }
    }
  }

  fun loadMissingImage(item: DiscoverListItem, force: Boolean) {

    fun updateShowsItem(newItem: DiscoverListItem) {
      val currentItems = uiState?.shows?.toMutableList()
      currentItems?.findReplace(newItem) { it.isSameAs(newItem) }
      uiState = DiscoverUiModel(shows = currentItems, scrollToTop = false)
    }

    viewModelScope.launch {
      updateShowsItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.show, item.image.type, force)
        updateShowsItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateShowsItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
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
