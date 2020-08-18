package com.michaldrabik.showly2.ui.watchlist

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.common.images.ShowImagesProvider
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.watchlist.cases.WatchlistEpisodesCase
import com.michaldrabik.showly2.ui.watchlist.cases.WatchlistLoadItemsCase
import com.michaldrabik.showly2.ui.watchlist.cases.WatchlistPinnedItemsCase
import com.michaldrabik.showly2.ui.watchlist.cases.WatchlistSortOrderCase
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.MessageEvent
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

class WatchlistViewModel @Inject constructor(
  private val loadItemsCase: WatchlistLoadItemsCase,
  private val pinnedItemsCase: WatchlistPinnedItemsCase,
  private val sortOrderCase: WatchlistSortOrderCase,
  private val episodesCase: WatchlistEpisodesCase,
  private val imagesProvider: ShowImagesProvider
) : BaseViewModel<WatchlistUiModel>() {

  private var searchQuery = ""

  fun loadWatchlist(resetScroll: Boolean = false) {
    viewModelScope.launch {
      val shows = loadItemsCase.loadMyShows()
      val items = shows.map { show ->
        async {
          val item = loadItemsCase.loadWatchlistItem(show)
          val image = imagesProvider.findCachedImage(show, ImageType.POSTER)
          item.copy(image = image)
        }
      }.awaitAll()

      val sortOrder = sortOrderCase.loadSortOrder()
      val allItems = loadItemsCase.prepareWatchlistItems(items, searchQuery, sortOrder)
      uiState =
        WatchlistUiModel(
          items = allItems,
          isSearching = searchQuery.isNotBlank(),
          sortOrder = sortOrder,
          resetScroll = resetScroll && sortOrder == SortOrder.RECENTLY_WATCHED
        )
    }
  }

  fun searchWatchlist(searchQuery: String) {
    this.searchQuery = searchQuery.trim()
    loadWatchlist()
  }

  fun setWatchedEpisode(context: Context, item: WatchlistItem) {
    viewModelScope.launch {
      if (!item.episode.hasAired(item.season)) {
        _messageLiveData.value = MessageEvent.info(R.string.errorEpisodeNotAired)
        return@launch
      }
      episodesCase.setEpisodeWatched(context, item)
      loadWatchlist(resetScroll = true)
    }
  }

  fun setSortOrder(sortOrder: SortOrder) {
    viewModelScope.launch {
      sortOrderCase.setSortOrder(sortOrder)
      loadWatchlist(resetScroll = true)
    }
  }

  fun togglePinItem(item: WatchlistItem) {
    if (item.isPinned) {
      pinnedItemsCase.removePinnedItem(item)
    } else {
      pinnedItemsCase.addPinnedItem(item)
    }
    loadWatchlist()
  }

  fun onOpenShowDetails() {
    uiState = WatchlistUiModel(resetScroll = false)
  }
}
