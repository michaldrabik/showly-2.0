package com.michaldrabik.showly2.ui.watchlist

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.EpisodeBundle
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.show.seasons.episodes.EpisodesManager
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.findReplace
import javax.inject.Inject
import kotlinx.coroutines.launch

class WatchlistViewModel @Inject constructor(
  private val interactor: WatchlistInteractor,
  private val episodesManager: EpisodesManager
) : BaseViewModel<WatchlistUiModel>() {

  fun loadWatchlist() {
    viewModelScope.launch {
      val items = interactor.loadWatchlist().map {
        val image = interactor.findCachedImage(it.show, POSTER)
        it.copy(image = image)
      }.toMutableList()

      val headerIndex = items.indexOfFirst { !it.isHeader() && !it.episode.hasAired(it.season) }
      if (headerIndex != -1) {
        val item = items[headerIndex]
        items.add(headerIndex, item.copy(headerTextResId = R.string.textWatchlistIncoming))
      }

      uiState = WatchlistUiModel(items)
    }
  }

  fun setWatchedEpisode(item: WatchlistItem) {
    viewModelScope.launch {
      if (!item.episode.hasAired(item.season)) {
        _messageStream.value = R.string.errorEpisodeNotAired
        return@launch
      }
      val bundle = EpisodeBundle(item.episode, item.season, item.show)
      episodesManager.setEpisodeWatched(bundle)
      loadWatchlist()
    }
  }

  fun findMissingImage(item: WatchlistItem, force: Boolean) {

    fun updateItem(new: WatchlistItem) {
      val currentItems = uiState?.items?.toMutableList()
      currentItems?.findReplace(new) { it.isSameAs(new) }
      uiState = WatchlistUiModel(items = currentItems)
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        updateItem(item.copy(image = image, isLoading = false))
      } catch (t: Throwable) {
        val unavailable = Image.createUnavailable(item.image.type)
        updateItem(item.copy(image = unavailable, isLoading = false))
      }
    }
  }
}
