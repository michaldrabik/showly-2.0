package com.michaldrabik.showly2.ui.watchlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.EpisodeBundle
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.show.seasons.episodes.EpisodesInteractor
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import kotlinx.coroutines.launch
import javax.inject.Inject

class WatchlistViewModel @Inject constructor(
  private val interactor: WatchlistInteractor,
  private val episodesInteractor: EpisodesInteractor
) : BaseViewModel() {

  val watchlistStream by lazy { MutableLiveData<List<WatchlistItem>>() }
  val uiStream by lazy { MutableLiveData<WatchlistUiModel>() }

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

      watchlistStream.value = items
    }
  }

  fun setWatchedEpisode(item: WatchlistItem) {
    viewModelScope.launch {
      if (!item.episode.hasAired(item.season)) {
        uiStream.value = WatchlistUiModel(info = R.string.errorEpisodeNotAired)
        clearStream()
        return@launch
      }
      val bundle = EpisodeBundle(item.episode, item.season, item.show)
      episodesInteractor.setEpisodeWatched(bundle)
      loadWatchlist()
    }
  }

  fun findMissingImage(item: WatchlistItem, force: Boolean) {
    viewModelScope.launch {
      uiStream.value = WatchlistUiModel(updateListItem = item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        uiStream.value = WatchlistUiModel(updateListItem = item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        val unavailable = Image.createUnavailable(item.image.type)
        uiStream.value = WatchlistUiModel(updateListItem = item.copy(isLoading = false, image = unavailable))
      }
    }
  }

  private fun clearStream() {
    uiStream.value = WatchlistUiModel()
  }
}