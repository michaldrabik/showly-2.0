package com.michaldrabik.ui_my_movies.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.common.ListViewMode
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.ReloadData
import com.michaldrabik.ui_base.events.TraktSyncAuthError
import com.michaldrabik.ui_base.events.TraktSyncError
import com.michaldrabik.ui_base.events.TraktSyncSuccess
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_movies.common.recycler.CollectionListItem
import com.michaldrabik.ui_my_movies.common.recycler.CollectionListItem.MovieItem
import com.michaldrabik.ui_my_movies.main.FollowedMoviesUiEvent.OpenPremium
import com.michaldrabik.ui_my_movies.main.FollowedMoviesUiState
import com.michaldrabik.ui_my_movies.watchlist.cases.WatchlistFiltersCase
import com.michaldrabik.ui_my_movies.watchlist.cases.WatchlistLoadMoviesCase
import com.michaldrabik.ui_my_movies.watchlist.cases.WatchlistSortOrderCase
import com.michaldrabik.ui_my_movies.watchlist.cases.WatchlistViewModeCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.michaldrabik.ui_base.events.Event as EventSync

@HiltViewModel
class WatchlistViewModel @Inject constructor(
  private val sortOrderCase: WatchlistSortOrderCase,
  private val filtersCase: WatchlistFiltersCase,
  private val loadMoviesCase: WatchlistLoadMoviesCase,
  private val viewModeCase: WatchlistViewModeCase,
  private val settingsRepository: SettingsRepository,
  private val imagesProvider: MovieImagesProvider,
  private val eventsManager: EventsManager,
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private var loadItemsJob: Job? = null

  private val itemsState = MutableStateFlow<List<CollectionListItem>>(emptyList())
  private val viewModeState = MutableStateFlow(ListViewMode.LIST_NORMAL)
  private val sortOrderState = MutableStateFlow<Event<Pair<SortOrder, SortType>>?>(null)
  private val scrollState = MutableStateFlow<Event<Boolean>?>(null)

  private var searchQuery: String? = null

  init {
    viewModelScope.launch { eventsManager.events.collect { onEvent(it) } }
  }

  fun onParentState(state: FollowedMoviesUiState) {
    when {
      this.searchQuery != state.searchQuery -> {
        this.searchQuery = state.searchQuery
        loadMovies(resetScroll = state.searchQuery.isNullOrBlank())
      }
    }
  }

  fun loadMovies(resetScroll: Boolean = false) {
    loadItemsJob?.cancel()
    loadItemsJob = viewModelScope.launch {
      viewModeState.value = viewModeCase.getListViewMode()
      itemsState.value = loadMoviesCase.loadMovies(searchQuery ?: "")
      scrollState.value = Event(resetScroll)
    }
  }

  fun setSortOrder(sortOrder: SortOrder, sortType: SortType) {
    viewModelScope.launch {
      sortOrderCase.setSortOrder(sortOrder, sortType)
      loadMovies(resetScroll = true)
    }
  }

  fun setFilters(isUpcoming: Boolean) {
    viewModelScope.launch {
      filtersCase.setIsUpcoming(isUpcoming)
      loadMovies(resetScroll = true)
    }
  }

  fun setNextViewMode() {
    if (settingsRepository.isPremium) {
      viewModeState.value = viewModeCase.setNextViewMode()
      return
    }
    viewModelScope.launch {
      eventChannel.send(OpenPremium)
    }
  }

  fun loadMissingImage(item: CollectionListItem, force: Boolean) {
    check(item is MovieItem)
    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.movie, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun loadMissingTranslation(item: CollectionListItem) {
    check(item is MovieItem)
    if (item.translation != null || settingsRepository.language == DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = loadMoviesCase.loadTranslation(item.movie, false)
        updateItem(item.copy(translation = translation))
      } catch (error: Throwable) {
        Timber.e(error)
      }
    }
  }

  private fun updateItem(item: CollectionListItem) {
    val currentItems = uiState.value.items.toMutableList()
    currentItems.findReplace(item) { it isSameAs item }
    itemsState.value = currentItems
  }

  private fun onEvent(event: EventSync) =
    when (event) {
      is TraktSyncSuccess -> loadMovies()
      is TraktSyncError -> loadMovies()
      is TraktSyncAuthError -> loadMovies()
      is ReloadData -> loadMovies()
      else -> Unit
    }

  val uiState = combine(
    itemsState,
    sortOrderState,
    scrollState,
    viewModeState
  ) { s1, s2, s3, s4 ->
    WatchlistUiState(
      items = s1,
      sortOrder = s2,
      resetScroll = s3,
      viewMode = s4
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = WatchlistUiState()
  )
}
