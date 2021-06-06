package com.michaldrabik.ui_my_shows.watchlist

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_my_shows.watchlist.cases.WatchlistLoadShowsCase
import com.michaldrabik.ui_my_shows.watchlist.cases.WatchlistRatingsCase
import com.michaldrabik.ui_my_shows.watchlist.cases.WatchlistSortOrderCase
import com.michaldrabik.ui_my_shows.watchlist.recycler.WatchlistListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
  private val sortOrderCase: WatchlistSortOrderCase,
  private val loadShowsCase: WatchlistLoadShowsCase,
  private val ratingsCase: WatchlistRatingsCase,
  private val imagesProvider: ShowImagesProvider
) : BaseViewModel<WatchlistUiModel>() {

  fun loadShows(resetScroll: Boolean = false) {
    viewModelScope.launch {
      val items = loadShowsCase.loadShows().map {
        val image = imagesProvider.findCachedImage(it.first, POSTER)
        WatchlistListItem(it.first, image, false, it.second)
      }
      uiState = WatchlistUiModel(items = items, resetScroll = ActionEvent(resetScroll))
      loadRatings(items, resetScroll)
    }
  }

  private fun loadRatings(items: List<WatchlistListItem>, resetScroll: Boolean) {
    if (items.isEmpty()) return
    viewModelScope.launch {
      try {
        val listItems = ratingsCase.loadRatings(items)
        uiState = WatchlistUiModel(items = listItems, resetScroll = ActionEvent(resetScroll))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "WatchlistViewModel::loadRatings()")
      }
    }
  }

  fun loadSortOrder() {
    viewModelScope.launch {
      val sortOrder = sortOrderCase.loadSortOrder()
      uiState = WatchlistUiModel(sortOrder = ActionEvent(sortOrder))
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

  fun setSortOrder(sortOrder: SortOrder) {
    viewModelScope.launch {
      sortOrderCase.setSortOrder(sortOrder)
      loadShows(resetScroll = true)
    }
  }

  private fun updateItem(new: WatchlistListItem) {
    val currentItems = uiState?.items?.toMutableList()
    currentItems?.findReplace(new) { it.isSameAs(new) }
    uiState = uiState?.copy(items = currentItems)
  }
}
