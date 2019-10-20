package com.michaldrabik.showly2.ui.watchlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.EpisodeBundle
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
  val uiStream by lazy { MutableLiveData<WatchlistUiModel>() } //TODO Use single event LiveData

  fun loadWatchlist() {
    viewModelScope.launch {
      val items = interactor.loadWatchlist().map {
        val image = interactor.findCachedImage(it.show, POSTER)
        it.copy(image = image)
      }
      watchlistStream.value = items
    }
  }

  fun setWatchedEpisode(item: WatchlistItem) {
    viewModelScope.launch {
      if (!item.episode.hasAired()) {
        uiStream.value = WatchlistUiModel(info = R.string.errorEpisodeNotAired)
        clearStream()
        return@launch
      }
      val bundle = EpisodeBundle(item.episode, item.season, item.show)
      episodesInteractor.setEpisodeWatched(bundle)
      loadWatchlist()
    }
  }

  private fun clearStream() {
    uiStream.value = WatchlistUiModel()
  }
}