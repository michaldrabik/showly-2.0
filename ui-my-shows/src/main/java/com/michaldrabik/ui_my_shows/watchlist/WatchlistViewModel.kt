package com.michaldrabik.ui_my_shows.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.main.FollowedShowsUiState
import com.michaldrabik.ui_my_shows.watchlist.cases.WatchlistLoadShowsCase
import com.michaldrabik.ui_my_shows.watchlist.cases.WatchlistRatingsCase
import com.michaldrabik.ui_my_shows.watchlist.cases.WatchlistSortOrderCase
import com.michaldrabik.ui_my_shows.watchlist.recycler.WatchlistListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
  private val sortOrderCase: WatchlistSortOrderCase,
  private val loadShowsCase: WatchlistLoadShowsCase,
  private val ratingsCase: WatchlistRatingsCase,
  private val imagesProvider: ShowImagesProvider,
) : ViewModel() {

  private var loadItemsJob: Job? = null

  private val itemsState = MutableStateFlow<List<WatchlistListItem>>(emptyList())
  private val sortOrderState = MutableStateFlow<Event<Pair<SortOrder, SortType>>?>(null)
  private val scrollState = MutableStateFlow<Event<Boolean>?>(null)

  private var searchQuery: String? = null

  fun onParentState(state: FollowedShowsUiState) {
    when {
      this.searchQuery != state.searchQuery -> {
        this.searchQuery = state.searchQuery
        loadShows(resetScroll = state.searchQuery.isNullOrBlank())
      }
    }
  }

  fun loadShows(resetScroll: Boolean = false) {
    loadItemsJob?.cancel()
    loadItemsJob = viewModelScope.launch {
      val items = loadShowsCase.loadShows(searchQuery ?: "")
        .map {
          val image = imagesProvider.findCachedImage(it.first, POSTER)
          WatchlistListItem(it.first, image, false, it.second)
        }
      itemsState.value = items
      scrollState.value = Event(resetScroll)
      loadRatings(items, resetScroll)
    }
  }

  private fun loadRatings(items: List<WatchlistListItem>, resetScroll: Boolean) {
    if (items.isEmpty()) return
    viewModelScope.launch {
      try {
        val listItems = ratingsCase.loadRatings(items)
        itemsState.value = listItems
        scrollState.value = Event(resetScroll)
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "WatchlistViewModel::loadRatings()")
      }
    }
  }

  fun loadSortOrder() {
    viewModelScope.launch {
      val sortOrder = sortOrderCase.loadSortOrder()
      sortOrderState.value = Event(sortOrder)
    }
  }

  fun setSortOrder(sortOrder: SortOrder, sortType: SortType) {
    viewModelScope.launch {
      sortOrderCase.setSortOrder(sortOrder, sortType)
      loadShows(resetScroll = true)
    }
  }

  fun loadMissingImage(item: WatchlistListItem, force: Boolean) {
    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.show, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun loadMissingTranslation(item: WatchlistListItem) {
    if (item.translation != null || loadShowsCase.language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = loadShowsCase.loadTranslation(item.show, false)
        updateItem(item.copy(translation = translation))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "WatchlistViewModel::loadMissingTranslation()")
      }
    }
  }

  private fun updateItem(new: WatchlistListItem) {
    val currentItems = uiState.value.items.toMutableList()
    currentItems.findReplace(new) { it.isSameAs(new) }
    itemsState.value = currentItems
  }

  val uiState = combine(
    itemsState,
    sortOrderState,
    scrollState
  ) { itemsState, sortOrderState, scrollState ->
    WatchlistUiState(
      items = itemsState,
      sortOrder = sortOrderState,
      resetScroll = scrollState
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = WatchlistUiState()
  )
}
