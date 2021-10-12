package com.michaldrabik.ui_my_movies.hidden

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_movies.hidden.cases.HiddenLoadMoviesCase
import com.michaldrabik.ui_my_movies.hidden.cases.HiddenRatingsCase
import com.michaldrabik.ui_my_movies.hidden.cases.HiddenSortOrderCase
import com.michaldrabik.ui_my_movies.hidden.recycler.HiddenListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HiddenViewModel @Inject constructor(
  private val sortOrderCase: HiddenSortOrderCase,
  private val loadMoviesCase: HiddenLoadMoviesCase,
  private val ratingsCase: HiddenRatingsCase,
  private val imagesProvider: MovieImagesProvider,
) : BaseViewModel() {

  private val itemsState = MutableStateFlow<List<HiddenListItem>>(emptyList())
  private val sortOrderState = MutableStateFlow<Event<SortOrder>?>(null)
  private val scrollState = MutableStateFlow<Event<Boolean>?>(null)

  val uiState = combine(
    itemsState,
    sortOrderState,
    scrollState
  ) { itemsState, sortOrderState, scrollState ->
    HiddenUiState(
      items = itemsState,
      sortOrder = sortOrderState,
      resetScroll = scrollState
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = HiddenUiState()
  )

  fun loadMovies(resetScroll: Boolean = false) {
    viewModelScope.launch {
      val items = loadMoviesCase.loadMovies().map {
        val image = imagesProvider.findCachedImage(it.first, POSTER)
        HiddenListItem(it.first, image, false, it.second)
      }
      itemsState.value = items
      scrollState.value = Event(resetScroll)
      loadRatings(items, resetScroll)
    }
  }

  private fun loadRatings(items: List<HiddenListItem>, resetScroll: Boolean) {
    if (items.isEmpty()) return
    viewModelScope.launch {
      try {
        val listItems = ratingsCase.loadRatings(items)
        itemsState.value = listItems
        scrollState.value = Event(resetScroll)
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "HiddenViewModel::loadRatings()")
      }
    }
  }

  fun loadSortOrder() {
    viewModelScope.launch {
      val sortOrder = sortOrderCase.loadSortOrder()
      sortOrderState.value = Event(sortOrder)
    }
  }

  fun loadMissingImage(item: HiddenListItem, force: Boolean) {
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

  fun loadMissingTranslation(item: HiddenListItem) {
    if (item.translation != null || loadMoviesCase.language == Config.DEFAULT_LANGUAGE) return
    viewModelScope.launch {
      try {
        val translation = loadMoviesCase.loadTranslation(item.movie, false)
        updateItem(item.copy(translation = translation))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "HiddenViewModel::loadMissingTranslation()")
      }
    }
  }

  fun setSortOrder(sortOrder: SortOrder) {
    viewModelScope.launch {
      sortOrderCase.setSortOrder(sortOrder)
      loadMovies(resetScroll = true)
    }
  }

  private fun updateItem(new: HiddenListItem) {
    val currentItems = uiState.value.items.toMutableList()
    currentItems.findReplace(new) { it isSameAs (new) }
    itemsState.value = currentItems
  }
}
