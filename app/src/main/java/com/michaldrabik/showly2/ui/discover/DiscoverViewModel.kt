package com.michaldrabik.showly2.ui.discover

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.FANART
import com.michaldrabik.showly2.model.ImageType.FANART_WIDE
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.ui.UiCache
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.discover.recycler.DiscoverListItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
  private val interactor: DiscoverInteractor,
  private val uiCache: UiCache
) : BaseViewModel() {

  val uiStream by lazy { MutableLiveData<DiscoverUiModel>() }

  fun loadTrendingShows(skipCache: Boolean = false) {
    viewModelScope.launch {
      val progress = launch {
        delay(500)
        uiStream.value = DiscoverUiModel(showLoading = true)
      }
      try {
        val shows = interactor.loadTrendingShows(skipCache)
        onShowsLoaded(shows)
      } catch (t: Throwable) {
        onError(Error(t))
      } finally {
        uiStream.value = DiscoverUiModel(showLoading = false)
        progress.cancel()
      }
    }
  }

  private suspend fun onShowsLoaded(shows: List<Show>) {
    val items = shows.mapIndexed { index, show ->
      val itemType =
        when (index) {
          in (0..500 step 14) -> FANART_WIDE
          in (5..500 step 14), in (9..500 step 14) -> FANART
          else -> POSTER
        }
      val image = interactor.findCachedImage(show, itemType)
      DiscoverListItem(show, image)
    }
    uiStream.value = DiscoverUiModel(trendingShows = items, listPosition = uiCache.discoverListPosition)
  }

  fun loadMissingImage(item: DiscoverListItem, force: Boolean) {
    viewModelScope.launch {
      uiStream.value = DiscoverUiModel(updateListItem = item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        uiStream.value =
          DiscoverUiModel(updateListItem = item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        uiStream.value =
          DiscoverUiModel(updateListItem = item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  fun saveListPosition(position: Int, offset: Int) {
    uiCache.discoverListPosition = Pair(position, offset)
  }

  private fun onError(error: Error) {
    uiStream.value = DiscoverUiModel(error = error)
  }
}