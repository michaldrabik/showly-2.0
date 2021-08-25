package com.michaldrabik.ui_my_movies.watchlist

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_movies.watchlist.cases.WatchlistLoadMoviesCase
import com.michaldrabik.ui_my_movies.watchlist.cases.WatchlistRatingsCase
import com.michaldrabik.ui_my_movies.watchlist.cases.WatchlistSortOrderCase
import com.michaldrabik.ui_my_movies.watchlist.recycler.WatchlistListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
  private val sortOrderCase: WatchlistSortOrderCase,
  private val ratingsCase: WatchlistRatingsCase,
  private val loadMoviesCase: WatchlistLoadMoviesCase,
  private val imagesProvider: MovieImagesProvider,
) : BaseViewModel() {

  private val itemsState = MutableStateFlow<List<WatchlistListItem>>(emptyList())
  private val sortOrderState = MutableStateFlow<ActionEvent<SortOrder>?>(null)
  private val scrollState = MutableStateFlow<ActionEvent<Boolean>?>(null)

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

  fun loadMovies(resetScroll: Boolean = false) {
    viewModelScope.launch {
      val dateFormat = loadMoviesCase.loadDateFormat()
      val items = loadMoviesCase.loadMovies().map {
        val image = imagesProvider.findCachedImage(it.first, POSTER)
        WatchlistListItem(it.first, image, false, it.second, null, dateFormat)
      }
      itemsState.value = items
      scrollState.value = ActionEvent(resetScroll)
      loadRatings(items, resetScroll)
    }
  }

  private fun loadRatings(items: List<WatchlistListItem>, resetScroll: Boolean) {
    if (items.isEmpty()) return
    viewModelScope.launch {
      try {
        val listItems = ratingsCase.loadRatings(items)
        itemsState.value = listItems
        scrollState.value = ActionEvent(resetScroll)
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "WatchlistViewModel::loadRatings()")
      }
    }
  }

  fun loadSortOrder() {
    viewModelScope.launch {
      val sortOrder = sortOrderCase.loadSortOrder()
      sortOrderState.value = ActionEvent(sortOrder)
    }
  }

  fun loadMissingImage(item: WatchlistListItem, force: Boolean) {
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

  fun loadMissingTranslation(item: WatchlistListItem) {
    if (item.translation != null || loadMoviesCase.language == DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = loadMoviesCase.loadTranslation(item.movie, false)
        updateItem(item.copy(translation = translation))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "WatchlistViewModel::loadMissingTranslation()")
      }
    }
  }

  fun setSortOrder(sortOrder: SortOrder) {
    viewModelScope.launch {
      sortOrderCase.setSortOrder(sortOrder)
      loadMovies(resetScroll = true)
    }
  }

  private fun updateItem(new: WatchlistListItem) {
    val currentItems = uiState.value.items.toMutableList()
    currentItems.findReplace(new) { it.isSameAs(new) }
    itemsState.value = currentItems
  }
}
