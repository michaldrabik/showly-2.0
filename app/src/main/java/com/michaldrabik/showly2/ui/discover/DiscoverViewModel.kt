package com.michaldrabik.showly2.ui.discover

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.common.images.ShowImagesProvider
import com.michaldrabik.showly2.model.DiscoverFilters
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.discover.cases.DiscoverFiltersCase
import com.michaldrabik.showly2.ui.discover.cases.DiscoverShowsCase
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import com.michaldrabik.showly2.utilities.MessageEvent
import com.michaldrabik.showly2.utilities.extensions.findReplace
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
  private val showsCase: DiscoverShowsCase,
  private val filtersCase: DiscoverFiltersCase,
  private val imagesProvider: ShowImagesProvider
) : BaseViewModel<DiscoverUiModel>() {

  private var lastPullToRefreshMs = 0L

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

        val shows = when {
          !pullToRefresh && !skipCache -> {
            showsCase.loadCachedShows(filters)
          }
          pullToRefresh || skipCache || !showsCase.isCacheValid() -> {
            showsCase.loadRemoteShows(filters)
          }
          else -> throw IllegalStateException("Unsupported case")
        }

        uiState = DiscoverUiModel(shows = shows, filters = filters, scrollToTop = scrollToTop)
        if (pullToRefresh) {
          lastPullToRefreshMs = nowUtcMillis()
        }
      } catch (t: Throwable) {
        onError()
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

  private fun onError() {
    _messageLiveData.value = MessageEvent.error(R.string.errorCouldNotLoadDiscover)
  }
}
