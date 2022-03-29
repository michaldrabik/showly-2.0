package com.michaldrabik.ui_discover_movies

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.trakt.TraktSyncStatusProvider
import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_discover_movies.cases.DiscoverFiltersCase
import com.michaldrabik.ui_discover_movies.cases.DiscoverMoviesCase
import com.michaldrabik.ui_discover_movies.recycler.DiscoverMovieListItem
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily.MOVIE
import com.michaldrabik.ui_model.ImageSource.TMDB
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
class DiscoverMoviesViewModel @Inject constructor(
  private val moviesCase: DiscoverMoviesCase,
  private val filtersCase: DiscoverFiltersCase,
  private val imagesProvider: MovieImagesProvider,
  private val syncStatusProvider: TraktSyncStatusProvider,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val itemsState = MutableStateFlow<List<DiscoverMovieListItem>?>(null)
  private val loadingState = MutableStateFlow(false)
  private val syncingState = MutableStateFlow(false)
  private val filtersState = MutableStateFlow<DiscoverFilters?>(null)
  private val scrollState = MutableStateFlow(Event(false))

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  var lastPullToRefreshMs = 0L

  init {
    viewModelScope.launch {
      syncStatusProvider.status.collect { syncingState.value = it }
    }
  }

  fun loadMovies(
    pullToRefresh: Boolean = false,
    resetScroll: Boolean = false,
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
          val movies = moviesCase.loadCachedMovies(filters)
          itemsState.value = movies
          filtersState.value = filters
          scrollState.value = Event(resetScroll)
        }

        if (pullToRefresh || skipCache || !moviesCase.isCacheValid()) {
          val movies = moviesCase.loadRemoteMovies(filters)
          itemsState.value = movies
          filtersState.value = filters
          scrollState.value = Event(resetScroll)
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

  fun loadMissingImage(item: DiscoverMovieListItem, force: Boolean) {

    fun updateItem(newItem: DiscoverMovieListItem) {
      val currentItems = uiState.value.items?.toMutableList()
      currentItems?.findReplace(newItem) { it isSameAs newItem }
      itemsState.value = currentItems
      scrollState.value = Event(false)
    }

    viewModelScope.launch {
      val loadingJob = launch {
        delay(750)
        updateItem(item.copy(isLoading = true))
      }
      try {
        val image = imagesProvider.loadRemoteImage(item.movie, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type, MOVIE, TMDB)))
        rethrowCancellation(t)
      } finally {
        loadingJob.cancel()
      }
    }
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
    DiscoverMoviesUiState(
      items = s1,
      isLoading = s2,
      isSyncing = s3,
      filters = s4,
      resetScroll = s5
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = DiscoverMoviesUiState()
  )
}
