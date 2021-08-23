package com.michaldrabik.ui_discover

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_discover.cases.DiscoverFiltersCase
import com.michaldrabik.ui_discover.cases.DiscoverShowsCase
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily.MOVIE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
  private val showsCase: DiscoverShowsCase,
  private val filtersCase: DiscoverFiltersCase,
  private val imagesProvider: ShowImagesProvider,
) : BaseViewModel() {

  private val itemsState = MutableStateFlow<List<DiscoverListItem>?>(null)
  private val loadingState = MutableStateFlow(false)
  private val filtersState = MutableStateFlow<DiscoverFilters?>(null)
  private val scrollState = MutableStateFlow(ActionEvent(false))

  val uiState = combine(
    itemsState,
    loadingState,
    filtersState,
    scrollState
  ) { s1, s2, s3, s4 ->
    DiscoverUiState(
      items = s1,
      isLoading = s2,
      filters = s3,
      resetScroll = s4
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = DiscoverUiState()
  )

  @VisibleForTesting(otherwise = PRIVATE)
  var lastPullToRefreshMs = 0L

  fun loadItems(
    pullToRefresh: Boolean = false,
    scrollToTop: Boolean = false,
    skipCache: Boolean = false,
    instantProgress: Boolean = false,
    newFilters: DiscoverFilters? = null,
  ) {
    loadingState.value = true

    if (pullToRefresh && nowUtcMillis() - lastPullToRefreshMs < Config.PULL_TO_REFRESH_COOLDOWN_MS) {
      loadingState.value = false
      return
    }

    loadingState.value = pullToRefresh

    viewModelScope.launch {
      val progressJob = launch {
        delay(if (pullToRefresh || instantProgress) 0 else 750)
        loadingState.value = true
      }

      try {
        newFilters?.let { filtersCase.saveFilters(it) }
        val filters = filtersCase.loadFilters()
        filtersState.value = filters

        if (!pullToRefresh && !skipCache) {
          val shows = showsCase.loadCachedShows(filters)
          itemsState.value = shows
          filtersState.value = filters
          scrollState.value = ActionEvent(scrollToTop)
        }

        if (pullToRefresh || skipCache || !showsCase.isCacheValid()) {
          val shows = showsCase.loadRemoteShows(filters)
          itemsState.value = shows
          filtersState.value = filters
          scrollState.value = ActionEvent(scrollToTop)
        }

        if (pullToRefresh) {
          lastPullToRefreshMs = nowUtcMillis()
        }
      } catch (error: Throwable) {
        onError(error)
      } finally {
        loadingState.value = false
        progressJob.cancel()
      }
    }
  }

  fun loadMissingImage(item: DiscoverListItem, force: Boolean) {

    fun updateItem(newItem: DiscoverListItem) {
      val currentItems = uiState.value.items?.toMutableList()
      currentItems?.findReplace(newItem) { it.isSameAs(newItem) }
      itemsState.value = currentItems
      scrollState.value = ActionEvent(false)
    }

    viewModelScope.launch {
      val loadingJob = launch {
        delay(750)
        updateItem(item.copy(isLoading = true))
      }
      try {
        val image = imagesProvider.loadRemoteImage(item.show, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type, MOVIE)))
        rethrowCancellation(t)
      } finally {
        loadingJob.cancel()
      }
    }
  }

  private suspend fun onError(error: Throwable) {
    if (error !is CancellationException) {
      _messageState.emit(MessageEvent.error(R.string.errorCouldNotLoadDiscover))
      Timber.e(error)
    }
    rethrowCancellation(error)
  }
}
