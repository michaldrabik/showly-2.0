package com.michaldrabik.ui_discover

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.ui_base.trakt.TraktSyncStatusProvider
import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_discover.cases.DiscoverFiltersCase
import com.michaldrabik.ui_discover.cases.DiscoverShowsCase
import com.michaldrabik.ui_discover.cases.DiscoverTwitterCase
import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily.MOVIE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
  private val showsCase: DiscoverShowsCase,
  private val filtersCase: DiscoverFiltersCase,
  private val twitterCase: DiscoverTwitterCase,
  private val imagesProvider: ShowImagesProvider,
  private val syncStatusProvider: TraktSyncStatusProvider,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val itemsState = MutableStateFlow<List<DiscoverListItem>?>(null)
  private val loadingState = MutableStateFlow(false)
  private val syncingState = MutableStateFlow(false)
  private val filtersState = MutableStateFlow<DiscoverFilters?>(null)
  private val scrollState = MutableStateFlow(Event(false))

  @VisibleForTesting(otherwise = PRIVATE)
  var lastPullToRefreshMs = 0L

  init {
    viewModelScope.launch {
      syncStatusProvider.status.collect { syncingState.value = it }
    }
  }

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
          scrollState.value = Event(scrollToTop)
        }

        if (pullToRefresh || skipCache || !showsCase.isCacheValid()) {
          val shows = showsCase.loadRemoteShows(filters)
          itemsState.value = shows
          filtersState.value = filters
          scrollState.value = Event(scrollToTop)
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
      scrollState.value = Event(false)
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

  fun cancelTwitterAd() {
    twitterCase.cancelTwitterAd()
    loadItems()
  }

  private suspend fun onError(error: Throwable) {
    if (error !is CancellationException) {
      messageChannel.send(MessageEvent.error(R.string.errorCouldNotLoadDiscover))
      Timber.e(error)
    }
    rethrowCancellation(error)
  }

  val uiState = combine(
    itemsState,
    loadingState,
    syncingState,
    filtersState,
    scrollState
  ) { s1, s2, s3, s4, s5 ->
    DiscoverUiState(
      items = s1,
      isLoading = s2,
      isSyncing = s3,
      filters = s4,
      resetScroll = s5
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = DiscoverUiState()
  )
}
